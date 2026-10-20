package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_254_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mordor", "51_254");

					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, vitality 2; a skirmish ability used only on purpose
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, vitality 2; Damage +1 only
					put("runner", "1_178");     // Goblin Runner: [moria] Orc; its when-played (2) does nothing when cheated in
					put("breaker", "8_97");     // Gorgoroth Breaker: [sauron] Orc; Archery: exert this minion (or stack it on a site you control) to make the fellowship archery total -2
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text

					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf archer companion; Archery: exert Legolas to wound a minion
					put("garrison", "8_36");    // Garrison of Gondor: [gondor] Man archer companion; defender only with a fortification
					put("arwen", "3_7");        // Arwen, Elven Rider: Elf companion, an archer only by the Elven Bow
					put("bow", "1_41");         // Elven Bow: bearer is an archer
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is not an archer
					put("orophin", "1_56");     // Orophin, Lorien Bowman: archer ally; Archery: exert Orophin to wound an Uruk-hai
					put("rohan", "17_93");      // Aragorn, Defender of Rohan: [rohan] Man companion, so a fit bearer for the javelin; his text does nothing unless the Ring-bearer is assigned to a skirmish
					put("javelin", "7_248");    // Rohirrim Javelin: Free Peoples possession; Archery: discard this possession to make the fellowship archery total +1
					put("doubleshot", "1_38");  // Double Shot: Archery event, spot an Elf archer companion to make the fellowship archery total +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MordorEnragedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Mordor Enraged
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype:
		 * Game Text: To play, exert a [sauron] minion. Plays on an archer companion.
		 * 	Each time the Free Peoples player uses an archery special ability, bearer must exert.
		 */

		var scn = GetScenario();

		var mordor = scn.GetFreepsCard("mordor");

		assertEquals("Mordor Enraged", mordor.getBlueprint().getTitle());
		assertNull(mordor.getBlueprint().getSubtitle());
		assertFalse(mordor.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, mordor.getBlueprint().getSide());
		assertEquals(Culture.SAURON, mordor.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, mordor.getBlueprint().getCardType());
		assertFalse(scn.HasKeyword(mordor, Keyword.SUPPORT_AREA));
		assertEquals(1, mordor.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsASauronMinionToPlayOnAnArcherCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mordor = scn.GetShadowCard("mordor");
		var soldier = scn.GetShadowCard("soldier");
		var sentry = scn.GetShadowCard("sentry");
		var runner = scn.GetShadowCard("runner");
		var legolas = scn.GetFreepsCard("legolas");
		var garrison = scn.GetFreepsCard("garrison");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveCardsToHand(mordor);
		scn.MoveCompanionsToTable(legolas, garrison, arwen, aragorn);
		scn.AttachCardsTo(arwen, scn.GetFreepsCard("bow"));
		scn.MoveAlliesToTable(orophin);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, sentry, runner);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(mordor));

		scn.ShadowPlayCard(mordor);
		// Archer companions of two cultures, and one who is an archer by a possession; not the archer ally, not the companion who is no archer.
		assertTrue(scn.ShadowHasCardChoiceAvailable(legolas, garrison, arwen));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(orophin, aragorn));
		scn.ShadowChooseCard(garrison);

		// A [sauron] Orc and a [sauron] Uruk-hai; not the [moria] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, sentry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(sentry);

		assertEquals(1, scn.GetWoundsOn(sentry));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertAttachedTo(mordor, garrison);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenTheOnlySauronMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mordor = scn.GetShadowCard("mordor");
		var soldier = scn.GetShadowCard("soldier");
		var runner = scn.GetShadowCard("runner");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCardsToHand(mordor);
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, runner);
		// Vitality 2 with 1 wound: exhausted.  The [moria] Orc beside it could exert.
		scn.AddWoundsToChar(soldier, 1);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(mordor));
		assertInHand(mordor);
	}

	@Test
	public void CannotBePlayedWithoutAnArcherCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mordor = scn.GetShadowCard("mordor");
		var soldier = scn.GetShadowCard("soldier");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveCardsToHand(mordor, legolas);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(orophin);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(mordor));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertInHand(mordor);
	}

	@Test
	public void BearerExertsEachTimeTheFreePeoplesPlayerUsesAnArcherySpecialAbility() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mordor = scn.GetShadowCard("mordor");
		var breaker = scn.GetShadowCard("breaker");
		var sentry = scn.GetShadowCard("sentry");
		var troop = scn.GetShadowCard("troop");
		var legolas = scn.GetFreepsCard("legolas");
		var garrison = scn.GetFreepsCard("garrison");
		var aragorn = scn.GetFreepsCard("aragorn");
		var orophin = scn.GetFreepsCard("orophin");
		var doubleshot = scn.GetFreepsCard("doubleshot");
		scn.MoveCompanionsToTable(legolas, garrison, aragorn);
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToHand(doubleshot);

		scn.StartGame();
		scn.AttachCardsTo(garrison, mordor);
		scn.MoveMinionsToTable(breaker, sentry, troop);
		scn.SkipToPhase(Phase.MANEUVER);

		// A Free Peoples special ability of another phase.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		scn.FreepsUseCardAction(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(garrison));

		scn.SkipToPhase(Phase.ARCHERY);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertEquals(0, scn.GetWoundsOn(garrison));

		// An archery event is not a special ability.
		scn.FreepsPlayCard(doubleshot);
		assertInDiscard(doubleshot);
		assertEquals(0, scn.GetWoundsOn(garrison));

		// The Shadow player's archery special ability.
		scn.ShadowUseCardAction(breaker);
		assertEquals(1, scn.GetWoundsOn(breaker));
		assertEquals(0, scn.GetWoundsOn(garrison));

		// Another companion's archery special ability: Legolas pays his own exertion, and the bearer exerts.
		scn.FreepsUseCardAction(legolas);
		scn.FreepsChooseCard(troop);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(garrison));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(orophin));

		scn.ShadowPass();

		// An ally's archery special ability, the second in the phase.
		scn.FreepsUseCardAction(orophin);
		assertEquals(1, scn.GetWoundsOn(sentry));
		assertEquals(1, scn.GetWoundsOn(orophin));
		assertEquals(2, scn.GetWoundsOn(garrison));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void BearerExertsWhenTheFreePeoplesPlayerUsesAnArcheryAbilityOfAPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mordor = scn.GetShadowCard("mordor");
		var troop = scn.GetShadowCard("troop");
		var garrison = scn.GetFreepsCard("garrison");
		var rohan = scn.GetFreepsCard("rohan");
		var javelin = scn.GetFreepsCard("javelin");
		scn.MoveCompanionsToTable(garrison, rohan);

		scn.StartGame();
		scn.AttachCardsTo(garrison, mordor);
		scn.AttachCardsTo(rohan, javelin);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);

		// The ability is a possession's, not a character's, and its user is neither the bearer nor the possession's own bearer.
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertEquals(0, scn.GetWoundsOn(garrison));
		assertAttachedTo(javelin, rohan);

		scn.FreepsUseCardAction(javelin);
		assertInDiscard(javelin);
		assertEquals(1, scn.GetWoundsOn(garrison));
		assertEquals(0, scn.GetWoundsOn(rohan));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}
}
