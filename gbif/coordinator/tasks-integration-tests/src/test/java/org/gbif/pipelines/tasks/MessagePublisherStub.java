package org.gbif.pipelines.tasks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.gbif.common.messaging.api.Message;
import org.gbif.common.messaging.api.MessagePublisher;

@Getter
@NoArgsConstructor(staticName = "create")
public class MessagePublisherStub implements MessagePublisher {

  private List<Message> messages = new ArrayList<>();

  @Override
  public void send(Message message) {
    messages.add(message);
  }

  @Override
  public void send(Message message, boolean b) {
    messages.add(message);
  }

  @Override
  public void send(Message message, String s) {
    messages.add(message);
  }

  @Override
  public void send(Object o, String s, String s1) {
    // NOP
  }

  @Override
  public void send(Object o, String s, String s1, boolean b) {
    // NOP
  }

  @Override
  public void replyToQueue(
      Object message, boolean persistent, String correlationId, String replyTo) {
    // NOP
  }

  @Override
  public <T> T sendAndReceive(Message message, String s, boolean b, String s1)
      throws IOException, InterruptedException {
    return null;
  }

  @Override
  public <T> T sendAndReceive(Object o, String s, String s1, boolean b, String s2)
      throws IOException, InterruptedException {
    return null;
  }

  @Override
  public void close() {
    messages = new ArrayList<>();
  }
}
