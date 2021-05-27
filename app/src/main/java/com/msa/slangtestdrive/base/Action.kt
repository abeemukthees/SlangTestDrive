package com.msa.slangtestdrive.base

/**
 * Created by Abhi Muktheeswarar on 27-May-2021
 */

interface Action

interface EventAction : Action

interface NavigateAction : Action

object EventConsumedAction : EventAction

object NavigateConsumedAction : NavigateAction