package com.example

import BeerPongProtocol._
import com.example.DomainModel.PlayerState

/**
 * Created by tombray on 5/11/15.
 */
trait HitOrMiss {
  private val rnd = new scala.util.Random
  def hitOrMiss(implicit state: PlayerState): BeerPongMsg = if (rnd.nextInt(state.totalBeersConsumed + 15 - state.cupsRemaining) == 0) Hit else Miss
}
