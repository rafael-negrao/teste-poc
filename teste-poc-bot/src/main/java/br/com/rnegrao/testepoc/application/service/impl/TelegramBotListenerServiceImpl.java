package br.com.rnegrao.testepoc.application.service.impl;

import br.com.rnegrao.testepoc.infra.service.TweetsServiceClient;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.request.SendChatAction;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.GetUpdatesResponse;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.pengrad.telegrambot.model.request.ChatAction.typing;
import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static java.util.Optional.of;

@Service
public class TelegramBotListenerServiceImpl {

    private final Logger logger = LoggerFactory.getLogger(TelegramBotListenerServiceImpl.class);

    @Autowired
    private TelegramBot telegramBot;

    private Integer offsetUpdates = 0;

    @Autowired
    private TweetsServiceClient tweetsServiceClient;

    @Scheduled(cron = "*/1 * * * * *")
    public void execute() {

        List<Update> updates = getUpdates();

        for (Update update : updates) {

            next(update);

            logger.info("Recebendo mensagem: {}", nonNull(update.message()) ? update.message().text() : "");

            sentTyping(update);

            sendMessage(update, "Preparando para consultar o resumo de hashtags");

            mostrarTop5(update);

            mostrarContagemTags(update);

            mostrarContagemTweetsPorHora(update);
        }
    }

    private void mostrarContagemTweetsPorHora(final Update update) {
        final StringBuilder s3 = new StringBuilder();
        s3.append("Contagem de Hashtags: %s");
        of(tweetsServiceClient.buscarContagemTweetsPorHora()).orElseGet(ArrayList::new).forEach(c -> {
            s3.append(format("\n%s, %s ", c.getCreatedAt(), c.getCountTweets()));
        });
        sendMessage(update, s3.toString());
    }

    private void mostrarContagemTags(final Update update) {
        final StringBuilder s2 = new StringBuilder();
        s2.append("Contagem de Hashtags: %s");
        of(tweetsServiceClient.buscarContagemHashtags()).orElseGet(ArrayList::new).forEach(c -> {
            s2.append(format("\n%s, %s ", c.getHashtag(), c.getCountTweets()));
        });
        sendMessage(update, s2.toString());
    }

    private void mostrarTop5(final Update update) {
        final StringBuilder s1 = new StringBuilder();
        s1.append("Top 5 usuários com mais seguidores: ");
        of(tweetsServiceClient.buscarTop5Usuarios()).orElseGet(ArrayList::new).forEach(u -> {
            u.getUsers().forEach((key, value) -> {
                s1.append(format("\n%s, %s ", value, key));
            });
        });
        sendMessage(update, s1.toString());
    }

    private void sendMessage(Update update, String mensagem) {
        final SendResponse sendResponse = telegramBot.execute(new SendMessage(update.message().chat().id(),mensagem));
        logger.info("Mensagem Enviada? {}", sendResponse.isOk());
    }

    private void sentTyping(Update update) {
        final BaseResponse baseResponse = telegramBot.execute(new SendChatAction(update.message().chat().id(), typing.name()));
        logger.info("Resposta de Chat Action Enviada? {}", baseResponse.isOk());
    }

    private void next(Update update) {
        offsetUpdates = update.updateId() + 1;
    }

    private List<Update> getUpdates() {
        GetUpdatesResponse updatesResponse = telegramBot.execute(new GetUpdates().limit(100).offset(offsetUpdates));
        return updatesResponse.updates();
    }
}
