/*
 *  Copyright (c) 2012 Malhar, Inc.
 *  All Rights Reserved.
 */
package com.malhartech.stream;

import com.malhartech.dag.MutatedSinkException;
import com.malhartech.dag.Sink;
import com.malhartech.dag.Stream;
import com.malhartech.dag.StreamConfiguration;
import com.malhartech.dag.StreamContext;

/**
 *
 * @author Chetan Narsude <chetan@malhar-inc.com>
 */

/**
 *
 * Inline streams are used for performance enhancement when both the nodes are in the same hadoop container<p>
 * <br>
 * Inline is a hint that the stram can choose to ignore. Stram may also convert a normal stream into an inline one
 * for performance reasons. A stream tagged with persist flag will not be inlined, as persistence requires a buffer
 * server<br>
 * Inline streams currently cannot be partitioned. Since the main reason for partitioning
 * is to load balance and that means across different hadoop containers. In future we may take a look at it.<br>
 * <br>
 *
 */

public class InlineStream implements Stream
{
    /**
     *
     */
  Sink current, output, shunted = new Sink()
  {
    @Override
    public void process(Object payload)
    {
    }
  };

  /**
   *
   * @param config
   */
  @Override
  public void setup(StreamConfiguration config)
  {
    // nothing to be done here.
  }

  /**
   *
   * @param context
   */
  @Override
  public void activate(StreamContext context)
  {
    current = output;
  }

  /**
   *
   */
  @Override
  public void deactivate()
  {
    current = shunted;
  }

  /**
   *
   */
  @Override
  public void teardown()
  {
    current = null;
  }

  /**
   *
   * @param port
   * @param sink
   * @return Sink
   */
  @Override
  public Sink connect(String port, Sink sink)
  {
    if (INPUT.equals(port)) {
      return this;
    }
    else {
      output = sink;
    }
    return null;
  }

  /**
   *
   * @param payload
   */
  @Override
  public final void process(Object payload)
  {
    try {
      current.process(payload);
    }
    catch (MutatedSinkException mse) {
      current = mse.getNewSink();
      current.process(payload);
      output = current;
    }
  }

  public final Sink getOutput()
  {
    return output;
  }

  @Override
  public boolean isMultiSinkCapable()
  {
    return false;
  }
}
