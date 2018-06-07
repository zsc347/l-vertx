package com.scaiz.vertx.container.impl;

import java.util.concurrent.Executor;
import java.util.LinkedList;

public class TaskQueue {
  
  private final LinkedList<Task> tasks = new LinkedList<>();
  private final Runnable runner;

  private Executor current;

  public TaskQueue() {
    this.runner = this::run;
  }

  private void run() {
    final Task task;
    synchronized (tasks) {
      task = tasks.poll();
      if (task == null) {
        current = null;
        return;
      }
      if (task.exec != current) {
        tasks.addFirst(task);
        task.exec.execute(runner);
        current = task.exec;
        return;
      }
    }
    try {
      task.runnable.run();
    } catch (Throwable t) {
      System.err.println("Caught unexpected exception " + t.getMessage());
    }
  }


  void execute(Runnable task, Executor exec) {
    synchronized (tasks) {
      tasks.add(new Task(task, exec));
      if (current == null) {
        current = exec;
        exec.execute(runner);
      }
    }
  }

  private static class Task {


    private final Runnable runnable;
    private final Executor exec;

    Task(Runnable runnable, Executor exec) {
      this.runnable = runnable;
      this.exec = exec;
    }
  }
}
