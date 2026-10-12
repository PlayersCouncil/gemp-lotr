package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.Skirmish;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.modifiers.CantBeOverwhelmedModifier;
import com.gempukku.lotro.logic.timing.AbstractEffect;
import com.gempukku.lotro.logic.timing.RuleUtils;
import com.gempukku.lotro.logic.timing.results.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
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
		if (fellowshipCharacter != null) {
			multiplier = game.getModifiersQuerying().getOverwhelmMultiplier(game, fellowshipCharacter);
		}

		if (fpStrength == 0 && shadowStrength == 0) {
			return Result.FELLOWSHIP_LOSES;
		} else if (multiplier < CantBeOverwhelmedModifier.ImmuneToOverwhelmThreshold
				&& fpStrength * multiplier <= shadowStrength) {
			return Result.FELLOWSHIP_OVERWHELMED;
		} else if (fpStrength <= shadowStrength) {
			return Result.FELLOWSHIP_LOSES;
		} else if (!RuleUtils.getMinionsFacingOverwhelm(game).isEmpty()) {
			// Minion by minion: at least one of the losing minions is overwhelmed (see RuleUtils)
			return Result.SHADOW_OVERWHELMED;
		} else {
			return Result.SHADOW_LOSES;
		}
	}

	/**
	 * The characters who will be overwhelmed if the skirmish resolves as it stands: the Free Peoples character, or
	 * those of the losing minions who are not spared by a protection of their own.
	 */
	public Set<PhysicalCard> getUpcomingOverwhelmed(LotroGame game) {
		return switch (getUpcomingResult(game)) {
			case FELLOWSHIP_OVERWHELMED -> fpList(game.getGameState().getSkirmish().getFellowshipCharacter());
			case SHADOW_OVERWHELMED -> RuleUtils.getMinionsFacingOverwhelm(game);
			default -> Collections.emptySet();
		};
	}

	@Override
	protected FullEffectResult playEffectReturningResult(LotroGame game) {
		Skirmish skirmish = game.getGameState().getSkirmish();

		Result result = getUpcomingResult(game);
		Set<PhysicalCard> overwhelmed = new HashSet<>(getUpcomingOverwhelmed(game));

		var shadow = skirmish.getShadowCharacters();
		var freeps = fpList(skirmish.getFellowshipCharacter());
		var removed = skirmish.getRemovedFromSkirmish();

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
			// A minion protected from being overwhelmed, beside others who are not, has only lost the skirmish
			Set<PhysicalCard> spared = new HashSet<>(losers);
			spared.removeAll(overwhelmed);
			if (!spared.isEmpty())
				game.getGameState().sendMessage(GameUtils.getAppendedNames(spared) + " lost the skirmish and "
						+ GameUtils.be(spared) + " not overwhelmed");
			game.getActionsEnvironment().emitEffectResult(new OverwhelmSkirmishResult(winners, losers, overwhelmed, skirmish.getRemovedFromSkirmish()));
		}
		else {
			game.getActionsEnvironment().emitEffectResult(new NormalSkirmishResult(winners, losers, skirmish.getRemovedFromSkirmish()));
		}

		for (PhysicalCard loser : losers) {
			SkirmishType loserType = (skirmishType == SkirmishType.OVERWHELM && !overwhelmed.contains(loser))
					? SkirmishType.NORMAL : skirmishType;
			game.getActionsEnvironment().emitEffectResult(new CharacterLostSkirmishResult(loserType, loser, involving));
		}
		// The losing characters with their strength as the skirmish resolves, handed to the winners (F-0480): a
		// winner's text that measures the losing character reads it as it was now, wherever it has gone by then.
		Map<PhysicalCard, Integer> loserStrengths = new LinkedHashMap<>();
		for (PhysicalCard loser : losers)
			loserStrengths.put(loser, game.getModifiersQuerying().getStrength(game, loser));
		// Legacy Ruling #2: The final removed character from a skirmish counts as a loser
		// https://wiki.lotrtcgpc.net/wiki/Legacy_Ruling_2
		if(losers.isEmpty()) {
			PhysicalCard finalRemoved = null;
			for(var card : removed) {
				finalRemoved = card;
			}
			if(finalRemoved != null) {
				game.getActionsEnvironment().emitEffectResult(new CharacterLostSkirmishResult(skirmishType, finalRemoved, involving));
				// Measured as it left the skirmish: it may be out of play by now
				Integer strengthWhenRemoved = skirmish.getStrengthWhenRemoved(finalRemoved);
				loserStrengths.put(finalRemoved, strengthWhenRemoved != null ? strengthWhenRemoved
						: game.getModifiersQuerying().getStrength(game, finalRemoved));
			}
		}
		for (PhysicalCard winner : winners) {
			game.getActionsEnvironment().emitEffectResult(new CharacterWonSkirmishResult(skirmishType, winner, involving, loserStrengths));
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
