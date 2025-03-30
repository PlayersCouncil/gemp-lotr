package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.Skirmish;
import com.gempukku.lotro.logic.timing.AbstractEffect;
import com.gempukku.lotro.logic.timing.RuleUtils;
import com.gempukku.lotro.logic.timing.results.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ResolveSkirmishEffect extends AbstractEffect {
	public enum Result {
		FELLOWSHIP_OVERWHELMED, FELLOWSHIP_LOSES, SHADOW_LOSES, SHADOW_OVERWHELMED
	}

	@Override
	public String getText(LotroGame game) {
		return "Resolve skirmish";
	}

	@Override
	public Type getType() {
		return Type.BEFORE_SKIRMISH_RESOLVED;
	}

	@Override
	public boolean isPlayableInFull(LotroGame game) {
		return true;
	}

	public Result getUpcomingResult(LotroGame game) {
		final Skirmish skirmish = game.getGameState().getSkirmish();

		if (skirmish.getShadowCharacters().isEmpty())
			return Result.SHADOW_LOSES;
		if (skirmish.getFellowshipCharacter() == null)
			return Result.FELLOWSHIP_LOSES;

		int fpStrength = RuleUtils.getFellowshipSkirmishStrength(game);
		int shadowStrength = RuleUtils.getShadowSkirmishStrength(game);

		final PhysicalCard fellowshipCharacter = skirmish.getFellowshipCharacter();

		int multiplier = 2;
		if (fellowshipCharacter != null)
			multiplier = game.getModifiersQuerying().getOverwhelmMultiplier(game, fellowshipCharacter);

		if (fpStrength == 0 && shadowStrength == 0)
			return Result.FELLOWSHIP_LOSES;
		else if (fpStrength * multiplier <= shadowStrength) {
			return Result.FELLOWSHIP_OVERWHELMED;
		} else if (fpStrength <= shadowStrength) {
			return Result.FELLOWSHIP_LOSES;
		} else if (fpStrength >= 2 * shadowStrength) {
			return Result.SHADOW_OVERWHELMED;
		} else {
			return Result.SHADOW_LOSES;
		}
	}

	@Override
	protected FullEffectResult playEffectReturningResult(LotroGame game) {
		Skirmish skirmish = game.getGameState().getSkirmish();

		Result result = getUpcomingResult(game);

		var shadow = skirmish.getShadowCharacters();
		var freeps = fpList(skirmish.getFellowshipCharacter());

		Set<PhysicalCard> involving = new HashSet<>();
		involving.addAll(freeps);
		involving.addAll(shadow);

		String message = null;
		Set<PhysicalCard> winners = null, losers = null;
		SkirmishType skirmishType = null;

		switch (result) {
			case FELLOWSHIP_LOSES -> {
				message = "Skirmish resolved: Shadow defeated Free Peoples";
				winners = shadow;
				losers = freeps;
				skirmishType = SkirmishType.NORMAL;
			}
			case FELLOWSHIP_OVERWHELMED -> {
				message = "Skirmish resolved: Shadow overwhelmed Free Peoples";
				winners = shadow;
				losers = freeps;
				skirmishType = SkirmishType.OVERWHELM;
			}
			case SHADOW_LOSES -> {
				message = "Skirmish resolved: Free Peoples defeated Shadow";
				winners = freeps;
				losers = shadow;
				skirmishType = SkirmishType.NORMAL;
			}
			case SHADOW_OVERWHELMED -> {
				message = "Skirmish resolved: Free Peoples overwhelmed Shadow";
				winners = freeps;
				losers = shadow;
				skirmishType = SkirmishType.OVERWHELM;
			}
		}

		game.getGameState().sendMessage(message);
		if(skirmishType == SkirmishType.OVERWHELM) {
			game.getActionsEnvironment().emitEffectResult(new OverwhelmSkirmishResult(winners, losers, skirmish.getRemovedFromSkirmish()));
		}
		else {
			game.getActionsEnvironment().emitEffectResult(new NormalSkirmishResult(winners, losers, skirmish.getRemovedFromSkirmish()));
		}

		for (PhysicalCard loser : losers) {
			game.getActionsEnvironment().emitEffectResult(new CharacterLostSkirmishResult(skirmishType, loser, involving));
		}
		for (PhysicalCard winner : winners) {
			game.getActionsEnvironment().emitEffectResult(new CharacterWonSkirmishResult(skirmishType, winner, involving));
		}

		return new FullEffectResult(true);
	}

	private Set<PhysicalCard> fpList(PhysicalCard card) {
		if (card != null)
			return Collections.singleton(card);
		else
			return Collections.emptySet();
	}
}
