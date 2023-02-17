package com.example.buns.service;

import com.example.buns.dal.entity.TypeSubscribe;
import com.example.buns.rest.model.Subscriber;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.groupadministration.BanChatMember;
import org.telegram.telegrambots.meta.api.methods.groupadministration.ExportChatInviteLink;
import org.telegram.telegrambots.meta.api.methods.groupadministration.UnbanChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Data
@Slf4j
public class TelegramBotHandler extends TelegramLongPollingBot {

    private final String ACCESS_LABEL = "Как получить доступ?";
    private final String DEMO_LABEL = "Хочу демо-доступ на 3 дня";

    private final SubscribersService subscribersService;


    private enum COMMANDS {
        INFO("/info"),
        START("/start"),
        DEMO("/demo"),
        ACCESS("/access"),
        SUCCESS("/success");


        private String command;

        COMMANDS(String command) {
            this.command = command;
        }

        public String getCommand() {
            return command;
        }
    }


    @Value("${telegram.support.chat-id}")
    private Long supportChatId;

    @Value("${telegram.name}")
    private String name;

    @Value("${telegram.token}")
    private String token;

    @Value("${telegram.chanel-id}")
    private Long privateChannelId;

    @Override
    public String getBotUsername() {
        return name;
    }

    @Override
    public String getBotToken() {
        return token;
    }


    @SneakyThrows
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            long chat_id = update.getMessage().getChatId();

            try {

                Pattern patternClear = Pattern.compile("^clear-expired$");
                Pattern patternGiveRights = Pattern.compile("^gr\\s(\\d*)$");
                Pattern patternGetInfo = Pattern.compile("^get-info\\s(\\d*)$");

                Matcher matcherClear = patternClear.matcher(text);
                Matcher matcherGiveRights = patternGiveRights.matcher(text);
                Matcher matcherGetInfo =  patternGetInfo.matcher(text);


                if (matcherClear.find() && isAdmin(chat_id)) {
                    clearExpired();
                } else if (matcherGiveRights.find() && isAdmin(chat_id)) {
                    giveRights(Long.valueOf(matcherGiveRights.group(1)));

                } else if (matcherGetInfo.find() && isAdmin(chat_id)) {
                    getInfo(Long.valueOf(matcherGetInfo.group(1)));
                } else {
                    SendMessage message = getCommandResponse(text, update.getMessage().getFrom(), chat_id);
                    message.enableHtml(true);
                    message.setParseMode(ParseMode.HTML);
                    message.setChatId(String.valueOf(chat_id));
                    execute(message);
                }
            } catch (TelegramApiException e) {
                e.printStackTrace();
                SendMessage message = handleNotFoundCommand();
                message.setChatId(String.valueOf(chat_id));
                try {
                    sendInfoToSupport("Error " + e.getMessage());

                    execute(message);
                } catch (TelegramApiException ex) {
                    ex.printStackTrace();
                }
            }
        }
        if (update.hasMessage() && update.getMessage().hasPhoto()) {
            String text = update.getMessage().getText();
            long chat_id = update.getMessage().getFrom().getId();

            // Message contains photo
            // Set variables

            List<PhotoSize> photos = update.getMessage().getPhoto();
            String f_id = Objects.requireNonNull(photos.stream().max(Comparator.comparing(PhotoSize::getFileSize))
                    .orElse(null)).getFileId();
            String caption;
            if (update.getMessage().getFrom().getUserName() == null) {
                caption = "Пользователь запросил полный доступ:\n" +
                        "\nName: " + update.getMessage().getFrom().getFirstName() + " "
                        + update.getMessage().getFrom().getLastName() +
                        "\nChat ID: " + chat_id + "";
            } else if (update.getMessage().getFrom().getLastName() == null && update.getMessage().getFrom().getUserName() == null) {
                caption = "Пользователь запросил полный доступ:\n" +
                        "\nName: " + update.getMessage().getFrom().getFirstName() +
                        "\nChat ID: " + chat_id + "";
            } else if (update.getMessage().getFrom().getLastName() == null) {
                caption = "Пользователь запросил полный доступ:\n" +
                        "\nLogin: @" + update.getMessage().getFrom().getUserName() +
                        "\nName: " + update.getMessage().getFrom().getFirstName() +
                        "\nChat ID: " + chat_id + "";
            } else {
                caption = "Пользователь запросил полный доступ:\n" +
                        "\nLogin: @" + update.getMessage().getFrom().getUserName() +
                        "\nName: " + update.getMessage().getFrom().getFirstName() + "" +
                        " " + update.getMessage().getFrom().getLastName() +
                        "\nChat ID: " + chat_id + "";
            }
            SendPhoto msg = new SendPhoto();
            msg.setChatId(supportChatId);

            msg.setPhoto(new InputFile(f_id));
            msg.setCaption(caption);

            SendMessage message = new SendMessage();
            message.setChatId(supportChatId);
            message.setText("Дать доступ?");
            message.setReplyMarkup(getKeybordForAdmin(chat_id));


            try {
                execute(msg);
                execute(message);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }

            SendMessage messageSuccess = new SendMessage();
            messageSuccess.setText("Ваши данные получены. Через некоторое время вы получите доступ.");
            messageSuccess.setChatId(String.valueOf(chat_id));

            try {
                execute(messageSuccess);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }


        } else if (update.hasCallbackQuery()) {

            try {
                if (StringUtils.isNumeric(update.getCallbackQuery().getData())) {
                    giveRights(Long.valueOf(update.getCallbackQuery().getData()));
                } else {
                    SendMessage message = getCommandResponse(update.getCallbackQuery().getData(), update.getCallbackQuery().getFrom(), update.getCallbackQuery().getMessage().getChatId());
                    message.enableHtml(true);
                    message.setParseMode(ParseMode.HTML);
                    message.setChatId(String.valueOf(update.getCallbackQuery().getMessage().getChatId()));
                    execute(message);
                }
            } catch (TelegramApiException e) {
                e.printStackTrace();
                sendInfoToSupport("Error " + e.getMessage());

            }
        }

    }


    private boolean isAdmin(Long chatId) {
        return chatId.equals(supportChatId);
    }

    public void clearExpired() throws TelegramApiException {
        List<Subscriber> subscribers = subscribersService.getExpired();
        List<Subscriber> successDeleted = new ArrayList<>();

        for (Subscriber subscriber : subscribers) {
            try {
                BanChatMember banChatMember = new BanChatMember();
                banChatMember.setChatId(String.valueOf(privateChannelId));
                banChatMember.setUserId((subscriber.getTelegramId()));
                execute(banChatMember);

                subscribersService.disable(subscriber.getId());

                SendMessage message = new SendMessage();
                message.setText("Ваш доступ к каналу окончен");
                message.setChatId(String.valueOf(subscriber.getTelegramId()));
                execute(message);

                successDeleted.add(subscriber);

                Thread.sleep(100);
            } catch (Exception ex) {
                ex.printStackTrace();
                sendInfoToSupport("Ошибка при удалении \nChat ID = " + subscriber.getTelegramId() + "\nID = " + subscriber.getId() + "\n" + ex.getMessage());
            }
        }
    }

    @SneakyThrows
    public void getInfo(Long chatId){
       Subscriber subscriber = subscribersService.getSubscriberByTelegramId(chatId);
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(subscriber);
        SendMessage message = new SendMessage();
        message.setText(json);
        message.setChatId(supportChatId);
        execute(message);

    }

    public void notifyExpiredSoon() throws TelegramApiException {
        List<Subscriber> subscribers1 = subscribersService.getExpiredIn1Day();


        for (Subscriber subscriber : subscribers1) {
            SendMessage message = new SendMessage();
            message.setText("Ваш доступ к каналу закончится через 1 день");
            message.setChatId(String.valueOf(subscriber.getTelegramId()));
            execute(message);

        }
        List<Subscriber> subscribers5 = subscribersService.getExpiredIn5Days();

        for (Subscriber subscriber : subscribers5) {
            SendMessage message = new SendMessage();
            message.setText("Ваш доступ к каналу закончится через 5 дней");
            message.setChatId(String.valueOf(subscriber.getTelegramId()));
            execute(message);

        }
        List<Subscriber> subscribers3 = subscribersService.getExpiredIn3Days();

        for (Subscriber subscriber : subscribers3) {
            SendMessage message = new SendMessage();
            message.setText("Ваш доступ к каналу закончится через 3 дня");
            message.setChatId(String.valueOf(subscriber.getTelegramId()));
            execute(message);

        }

    }

    private void giveRights(Long chatId) throws TelegramApiException {

        SendMessage messageSupport = new SendMessage();

        messageSupport.setChatId(String.valueOf(supportChatId));


        try {
            UnbanChatMember unbanChatMember = new UnbanChatMember();
            unbanChatMember.setChatId(privateChannelId);
            unbanChatMember.setOnlyIfBanned(true);
            unbanChatMember.setUserId(chatId);

            execute(unbanChatMember);
        } catch (TelegramApiException e) {
            messageSupport.setText("Ощибка при удалении пользователя из бана: " + e.getMessage());
            execute(messageSupport);
        }
        addInfoSubscriberToDb(null, chatId, null, TypeSubscribe.FULL);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Вам выдан полный доступ: " + getChatInviteLink());
        execute(message);

        messageSupport.setText("Выдан полный доступ для пользователя " + chatId);
        execute(messageSupport);
    }

    private void sendInfoToSupport(String message) throws TelegramApiException {
        SendMessage messageSupport = new SendMessage();
        messageSupport.setText(message);
        messageSupport.setChatId(String.valueOf(supportChatId));

        execute(messageSupport);
    }

    private SendMessage getCommandResponse(String text, User user, Long chatId) throws TelegramApiException {

        if (text.equals(COMMANDS.ACCESS.getCommand())) {
            return handleAccessCommand();
        }


        if (text.equals(COMMANDS.START.getCommand())) {
            return handleStartCommand(user.getUserName(), user.getFirstName(), chatId);
        }

        if (text.equals(COMMANDS.DEMO.getCommand())) {
            return handleDemoCommand(user.getUserName(), user.getFirstName(), chatId);
        }


        return handleNotFoundCommand();
    }


    private SendMessage handleNotFoundCommand() {
        SendMessage message = new SendMessage();
        message.setText("Вы что-то сделали не так. Выберите команду:");
        message.setReplyMarkup(getKeyboard());
        return message;
    }

    private String getChatInviteLink() throws TelegramApiException {
        ExportChatInviteLink exportChatInviteLink = new ExportChatInviteLink();
        exportChatInviteLink.setChatId(String.valueOf(privateChannelId));
        return execute(exportChatInviteLink);
    }

    private SendMessage handleDemoCommand(String username, String name, Long chatId) throws TelegramApiException {
        SendMessage message = new SendMessage();

        if (subscribersService.isDemoAccess(chatId)) {
            message.setText("Ссылка для доступа к закрытому каналу: " + getChatInviteLink() + " \nЧерез 3 дня вы будете исключены из канала");

            addInfoSubscriberToDb(username, chatId, name, TypeSubscribe.DEMO);
        } else {
            message.setText("Вы уже получали демо-доступ");
        }

        message.setReplyMarkup(getKeyboard());

        return message;
    }

    private void addInfoSubscriberToDb(String username, Long chatId, String name, TypeSubscribe typeSubscribe) {


        Subscriber subscriber = new Subscriber();
        subscriber.setTypeSubscribe(typeSubscribe);
        subscriber.setTelegramId(chatId);
        subscriber.setName(name);
        subscriber.setLogin(username);
        subscriber.setStartDate(LocalDateTime.now());
        if (subscribersService.isInDb(chatId)) {
            subscribersService.add(subscriber);

        } else {
            Long id = subscribersService.getSubscriberByTelegramId(chatId).getId();
            subscribersService.update(id,LocalDateTime.now().plusDays(30));
        }
    }


    private SendMessage handleStartCommand(String username, String name, Long chatId) {
        SendMessage message = new SendMessage();
        message.setText("Доступные команды:");
        message.setReplyMarkup(getKeyboard());
        return message;
    }


    private SendMessage handleAccessCommand() {
        SendMessage message = new SendMessage();
        message.setText("Чтобы получить полный доступ, вам необходимо отправить скриншот, подтверждающий оплату.");
        message.setReplyMarkup(getKeyboard());
        return message;
    }

    private InlineKeyboardMarkup getKeyboard() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        InlineKeyboardButton inlineKeyboardButtonAccess = new InlineKeyboardButton();
        inlineKeyboardButtonAccess.setText(ACCESS_LABEL);
        inlineKeyboardButtonAccess.setCallbackData(COMMANDS.ACCESS.getCommand());

        InlineKeyboardButton inlineKeyboardButtonDemo = new InlineKeyboardButton();
        inlineKeyboardButtonDemo.setText(DEMO_LABEL);
        inlineKeyboardButtonDemo.setCallbackData(COMMANDS.DEMO.getCommand());

        List<List<InlineKeyboardButton>> keyboardButtons = new ArrayList<>();

        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        keyboardButtonsRow1.add(inlineKeyboardButtonAccess);

        List<InlineKeyboardButton> keyboardButtonsRow2 = new ArrayList<>();
        keyboardButtonsRow2.add(inlineKeyboardButtonDemo);

        keyboardButtons.add(keyboardButtonsRow1);
        keyboardButtons.add(keyboardButtonsRow2);


        inlineKeyboardMarkup.setKeyboard(keyboardButtons);

        return inlineKeyboardMarkup;
    }

    private InlineKeyboardMarkup getKeybordForAdmin(Long chatId) throws TelegramApiException {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineKeyboardButtonRights = new InlineKeyboardButton();
        inlineKeyboardButtonRights.setText("Дать доступ");
        inlineKeyboardButtonRights.setCallbackData(String.valueOf(chatId));

        List<List<InlineKeyboardButton>> keyboardButtons = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        keyboardButtonsRow1.add(inlineKeyboardButtonRights);
        keyboardButtons.add(keyboardButtonsRow1);
        inlineKeyboardMarkup.setKeyboard(keyboardButtons);

        return inlineKeyboardMarkup;
    }

    @Component
    class Scheduler {

        //fixedRate in milliseconds 3600000 * 24 = 1 day
        @Scheduled(fixedRate = 3600000 * 24)
        public void reportCurrentData() throws TelegramApiException {
            clearExpired();
            notifyExpiredSoon();
        }
    }

}
