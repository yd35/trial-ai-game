package nz.ac.auckland.se206;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.util.Duration;

public class SharedTimer {
  private static SharedTimer instance;
  private IntegerProperty seconds;

  public static SharedTimer initializeTimer(int time) {
    instance = new SharedTimer(time);
    return instance;
  }

  private Timeline timeline;

  private SharedTimer(int time) {
    seconds = new SimpleIntegerProperty(time);
    timeline =
        new Timeline(
            new KeyFrame(
                Duration.seconds(1),
                e -> {
                  if (instance.seconds.get() > 0) {
                    instance.seconds.set(instance.seconds.get() - 1);
                  } else {
                    instance.timeline.stop();
                  }
                }));
    timeline.setCycleCount(Timeline.INDEFINITE);
  }

  public static SharedTimer getInstance() {
    return instance;
  }

  public void start() {
    timeline.play();
  }

  public IntegerProperty secondsProperty() {
    return seconds;
  }

  public int getSeconds() {
    return seconds.get();
  }

  public void stop() {
    timeline.stop();
  }

  // resets the timer to a specific time instead of creating a new instance of the timer
  public static void reset(int time) {
    instance.stop();
    instance.seconds.set(time);
    instance.timeline.stop();
    instance.timeline.getKeyFrames().clear();
    instance
        .timeline
        .getKeyFrames()
        .add(
            new KeyFrame(
                Duration.seconds(1),
                e -> {
                  if (instance.seconds.get() > 0) {
                    instance.seconds.set(instance.seconds.get() - 1);
                  } else {
                    instance.timeline.stop();
                  }
                }));
    instance.timeline.setCycleCount(Timeline.INDEFINITE);
  }
}
