package com.gempukku.lotro.logic.effects;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.Skirmish;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.modifiers.CantBeOverwhelmedModifier;
import com.gempukku.lotro.logic.timing.AbstractEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.RuleUtils;
import com.gempukku.lotro.logic.timing.UnrespondableEffect;
import com.gempukku.lotro.logic.timing.results.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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

	// The decision of step 3, kept for step 5 (null until the skirmish has resolved)
	private Set<PhysicalCard> _winners;
	private Set<PhysicalCard> _losers;
	private Set<PhysicalCard> _overwhelmed;
	private Set<PhysicalCard> _involving;
	private SkirmishType _skirmishType;
	private PhysicalCard _finalRemoved;

	/**
	 * Steps 3 and 4 of the skirmish procedure (the Rules Council's draft for CR "skirmish phase", binding: Ketura,
	 * 2026-10-16 and 2026-10-19): the strengths are compared and the result decided, and the winners win.  Only the
	 * winners' results are emitted here, so that everything that answers the win, required, optional or a
	 * Response, is carried out before the losers lose.  The losers lose at step 5, in the effect
	 * {@link #getLossStep()} gives, which plays the decision kept here.
	 */
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

		// The losers as decided now, whatever befalls them at step 4
		_winners = winners;
		_losers = new LinkedHashSet<>(losers);
		_overwhelmed = overwhelmed;
		_involving = involving;
		_skirmishType = skirmishType;

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
				_finalRemoved = finalRemoved;
				// Measured as it left the skirmish: it may be out of play by now
				Integer strengthWhenRemoved = skirmish.getStrengthWhenRemoved(finalRemoved);
				loserStrengths.put(finalRemoved, strengthWhenRemoved != null ? strengthWhenRemoved
						: game.getModifiersQuerying().getStrength(game, finalRemoved));
			}
		}
		// Step 4: the winners win
		for (PhysicalCard winner : winners) {
			game.getActionsEnvironment().emitEffectResult(new CharacterWonSkirmishResult(skirmishType, winner, involving, loserStrengths));
		}

		return new FullEffectResult(true);
	}

	/**
	 * Step 5 of the skirmish procedure, the losers lose, to be carried out once everything that answers step 4 has
	 * been: the losers decided at step 3 lose the skirmish, and those of them still in it take the loss effects (5B,
	 * the overwhelm kill; 5C, the wounds), with what answers the loss, ordered by the Free Peoples player as one
	 * moment.  The overwhelm is the one decided at step 3 ("was at least double"): a strength changed at step 4
	 * neither makes nor undoes one.  5A: a loser who has left the skirmish, or play, by now takes no loss effects,
	 * and with no loser left in it none is resolved at all.  Does nothing if the skirmish did not resolve.
	 */
	public Effect getLossStep() {
		return new UnrespondableEffect() {
			@Override
			protected void doPlayEffect(LotroGame game) {
				if (_losers != null)
					playLossStep(game);
			}
		};
	}

	private void playLossStep(LotroGame game) {
		Skirmish skirmish = game.getGameState().getSkirmish();
		// 5A: only the losers still in the skirmish take the loss effects
		Set<PhysicalCard> inSkirmish = new LinkedHashSet<>();
		for (PhysicalCard loser : _losers) {
			if (skirmish.getFellowshipCharacter() == loser || skirmish.getShadowCharacters().contains(loser))
				inSkirmish.add(loser);
		}

		if (!inSkirmish.isEmpty()) {
			if (_skirmishType == SkirmishType.OVERWHELM) {
				// A minion protected from being overwhelmed, beside others who are not, has only lost the skirmish
				Set<PhysicalCard> spared = new HashSet<>(inSkirmish);
				spared.removeAll(_overwhelmed);
				if (!spared.isEmpty())
					game.getGameState().sendMessage(GameUtils.getAppendedNames(spared) + " lost the skirmish and "
							+ GameUtils.be(spared) + " not overwhelmed");
				game.getActionsEnvironment().emitEffectResult(new OverwhelmSkirmishResult(_winners, inSkirmish, _overwhelmed, skirmish.getRemovedFromSkirmish()));
			} else {
				game.getActionsEnvironment().emitEffectResult(new NormalSkirmishResult(_winners, inSkirmish, skirmish.getRemovedFromSkirmish()));
			}
		}

		// Every loser decided at step 3 loses the skirmish, whether or not he is still in it (step 5 comes before
		// its 5A), and Legacy Ruling 2's final removed character where that side was emptied before step 3
		for (PhysicalCard loser : _losers) {
			SkirmishType loserType = (_skirmishType == SkirmishType.OVERWHELM && !_overwhelmed.contains(loser))
					? SkirmishType.NORMAL : _skirmishType;
			game.getActionsEnvironment().emitEffectResult(new CharacterLostSkirmishResult(loserType, loser, _involving));
		}
		if (_losers.isEmpty() && _finalRemoved != null)
			game.getActionsEnvironment().emitEffectResult(new CharacterLostSkirmishResult(_skirmishType, _finalRemoved, _involving));
	}

	private Set<PhysicalCard> fpList(PhysicalCard card) {
		if (card != null)
			return Collections.singleton(card);
		else
			return Collections.emptySet();
	}
}
