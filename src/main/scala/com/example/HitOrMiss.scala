package com.example

import BeerPongProtocol._
/**
 * Created by tombray on 5/11/15.
 */
trait HitOrMiss {
  private val rnd = new scala.util.Random
  def hitOrMiss(cups: Int, totalBeersConsumed: Int): BeerPongMsg = if (rnd.nextInt(totalBeersConsumed + 15 - cups) == 0) Hit else Miss
}
