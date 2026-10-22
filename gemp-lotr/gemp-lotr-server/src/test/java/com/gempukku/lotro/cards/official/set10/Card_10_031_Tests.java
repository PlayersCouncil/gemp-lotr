package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_031_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gain", "10_31");
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man companion, Knight and no other text
					put("merchant", "7_97");    // Gondorian Merchant: a [gondor] Man ally
					put("oathbreaker", "8_41"); // Oathbreaker: a [gondor] companion who is no Man (a Wraith)
					put("guard", "5_83");       // Household Guard: a Man companion who is not [gondor] ([rohan])
					put("shards", "3_44");      // The Shards of Narsil: [gondor] support artifact that play stacks [gondor] cards on
					put("firstlevel", "7_92");  // First Level: a [gondor] fortification its ability moves onto a minion
					put("secondlevel", "7_118");// Second Level: another, of another title

					put("teeth", "5_110");      // Teeth of Mordor: [sauron] support condition that play stacks [sauron] Orcs on
					put("hunter", "1_256");     // Morgul Hunter: a [sauron] Orc, to be stacked there
					put("scaffolding", "6_73"); // Scaffolding: [isengard] support POSSESSION that play stacks [isengard] Orcs on
					put("tender", "6_70");      // Isengard Tender: an [isengard] Orc, to be stacked there
					put("runner", "1_178");     // Goblin Runner: a [moria] Orc of vitality 1, exhausted as he stands
					put("corpse", "6_99");      // Corpse Lights: a [sauron] Wraith minion of vitality 2
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc with no text
					put("scimitar", "1_180");   // Goblin Scimitar: a [moria] possession for him that is no fortification
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EveryLittleisaGainStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Every Little is a Gain
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Spot a [gondor] Man to discard a stacked Shadow card or to wound a minion bearing a fortification.
		*/

		var scn = GetScenario();

		var gain = scn.GetFreepsCard("gain");

		assertEquals("Every Little is a Gain", gain.getBlueprint().getTitle());
		assertNull(gain.getBlueprint().getSubtitle());
		assertFalse(gain.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, gain.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, gain.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, gain.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(gain, Timeword.MANEUVER));
		assertEquals(1, gain.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsNotPlayableWithoutAGondorManAndIsPlayableWithTheGondorAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gain = scn.GetFreepsCard("gain");
		var oathbreaker = scn.GetFreepsCard("oathbreaker");
		var guard = scn.GetFreepsCard("guard");
		var merchant = scn.GetFreepsCard("merchant");
		var teeth = scn.GetShadowCard("teeth");
		var hunter = scn.GetShadowCard("hunter");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(oathbreaker, guard);
		scn.MoveCardsToHand(gain);

		scn.StartGame();
		scn.MoveCardsToSupportArea(teeth);
		scn.StackCardsOn(teeth, hunter);
		scn.MoveMinionsToTable(troop);
		assertInZone(Zone.STACKED, hunter);
		scn.SkipToPhase(Phase.MANEUVER);

		// A [gondor] companion who is no Man and a Man who is not [gondor]: nothing to spot.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(gain));

		// The [gondor] Man ally is enough.
		scn.MoveAlliesToTable(merchant);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(gain));
		scn.FreepsPlayCard(gain);

		// No minion bears a fortification, so the discard is what is done, unasked.
		assertInDiscard(hunter);
		assertInDiscard(gain);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void DiscardsAStackedShadowCardOfThePlayersChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gain = scn.GetFreepsCard("gain");
		var knight = scn.GetFreepsCard("knight");
		var shards = scn.GetFreepsCard("shards");
		var stackedGondor = scn.GetFreepsCard("oathbreaker");
		var firstLevel = scn.GetFreepsCard("firstlevel");
		var teeth = scn.GetShadowCard("teeth");
		var hunter = scn.GetShadowCard("hunter");
		var scaffolding = scn.GetShadowCard("scaffolding");
		var tender = scn.GetShadowCard("tender");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToSupportArea(shards);
		scn.StackCardsOn(shards, stackedGondor);
		scn.MoveCardsToHand(gain);

		scn.StartGame();
		scn.MoveCardsToSupportArea(teeth, scaffolding);
		scn.StackCardsOn(teeth, hunter);
		scn.StackCardsOn(scaffolding, tender);
		scn.MoveMinionsToTable(troop);
		scn.AttachCardsTo(troop, firstLevel);
		assertInZone(Zone.STACKED, hunter, tender, stackedGondor);
		assertAttachedTo(firstLevel, troop);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(gain);
		// Both can be done here, so the player is asked which.
		assertTrue(scn.FreepsChoiceAvailable("Discard"));
		assertTrue(scn.FreepsChoiceAvailable("Wound"));
		scn.FreepsChoose("Discard");

		// Shadow cards stacked on a Shadow condition and on a Shadow possession, of two cultures; not a Free Peoples
		// card stacked, nor a Shadow card in play.
		assertTrue(scn.FreepsHasCardChoiceAvailable(hunter, tender));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(stackedGondor, teeth, troop));
		scn.FreepsChooseCard(hunter);

		assertInDiscard(hunter);
		assertInZone(Zone.STACKED, tender, stackedGondor);
		assertInZone(Zone.SUPPORT, teeth);
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WoundsAMinionBearingAFortificationOfThePlayersChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gain = scn.GetFreepsCard("gain");
		var knight = scn.GetFreepsCard("knight");
		var firstLevel = scn.GetFreepsCard("firstlevel");
		var secondLevel = scn.GetFreepsCard("secondlevel");
		var teeth = scn.GetShadowCard("teeth");
		var hunter = scn.GetShadowCard("hunter");
		var runner = scn.GetShadowCard("runner");
		var corpse = scn.GetShadowCard("corpse");
		var troop = scn.GetShadowCard("troop");
		var scimitar = scn.GetShadowCard("scimitar");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToHand(gain);

		scn.StartGame();
		scn.MoveCardsToSupportArea(teeth);
		scn.StackCardsOn(teeth, hunter);
		scn.MoveMinionsToTable(runner, corpse, troop);
		scn.AttachCardsTo(runner, firstLevel);
		scn.AttachCardsTo(corpse, secondLevel);
		scn.AttachCardsTo(troop, scimitar);
		assertAttachedTo(firstLevel, runner);
		assertAttachedTo(secondLevel, corpse);
		assertAttachedTo(scimitar, troop);
		assertInZone(Zone.STACKED, hunter);
		assertEquals(0, scn.GetWoundsOn(corpse));
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(gain);
		scn.FreepsChoose("Wound");

		// A [moria] Orc and a [sauron] Wraith, each bearing a fortification; not a minion bearing a possession that is none.
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, corpse));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(runner);

		// A wound, not an exertion: the Runner, of vitality 1, takes it and is killed.
		assertInDiscard(runner);
		assertEquals(0, scn.GetWoundsOn(corpse));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertInZone(Zone.STACKED, hunter);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoStackedShadowCardTheFortifiedMinionIsWoundedWithoutAChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gain = scn.GetFreepsCard("gain");
		var knight = scn.GetFreepsCard("knight");
		var shards = scn.GetFreepsCard("shards");
		var stackedGondor = scn.GetFreepsCard("oathbreaker");
		var secondLevel = scn.GetFreepsCard("secondlevel");
		var corpse = scn.GetShadowCard("corpse");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToSupportArea(shards);
		// A stacked card, but a Free Peoples one.
		scn.StackCardsOn(shards, stackedGondor);
		scn.MoveCardsToHand(gain);

		scn.StartGame();
		scn.MoveMinionsToTable(corpse);
		scn.AttachCardsTo(corpse, secondLevel);
		assertInZone(Zone.STACKED, stackedGondor);
		assertEquals(0, scn.GetWoundsOn(corpse));
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(gain);

		assertFalse(scn.FreepsChoiceAvailable("Discard"));
		assertEquals(1, scn.GetWoundsOn(corpse));
		assertInZone(Zone.STACKED, stackedGondor);
		assertInDiscard(gain);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoMinionBearingAFortificationTheStackedShadowCardIsDiscardedWithoutAChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gain = scn.GetFreepsCard("gain");
		var knight = scn.GetFreepsCard("knight");
		var teeth = scn.GetShadowCard("teeth");
		var hunter = scn.GetShadowCard("hunter");
		var troop = scn.GetShadowCard("troop");
		var scimitar = scn.GetShadowCard("scimitar");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToHand(gain);

		scn.StartGame();
		scn.MoveCardsToSupportArea(teeth);
		scn.StackCardsOn(teeth, hunter);
		scn.MoveMinionsToTable(troop);
		scn.AttachCardsTo(troop, scimitar);
		assertInZone(Zone.STACKED, hunter);
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(gain);

		assertFalse(scn.FreepsChoiceAvailable("Wound"));
		assertInDiscard(hunter);
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNeitherTargetItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gain = scn.GetFreepsCard("gain");
		var knight = scn.GetFreepsCard("knight");
		var teeth = scn.GetShadowCard("teeth");
		var troop = scn.GetShadowCard("troop");
		var scimitar = scn.GetShadowCard("scimitar");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToHand(gain);

		scn.StartGame();
		scn.MoveCardsToSupportArea(teeth);
		scn.MoveMinionsToTable(troop);
		scn.AttachCardsTo(troop, scimitar);
		assertEquals(0, scn.GetStackedCards(teeth).size());
		assertInZone(Zone.SUPPORT, teeth);
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.SkipToPhase(Phase.MANEUVER);

		// An event's targets are no requirement to play it (CR 4.2 "effect"): it is played, and asks nothing.
		assertTrue(scn.FreepsPlayAvailable(gain));
		scn.FreepsPlayCard(gain);

		// Neither branch has anything to act on, so the player is not asked which (F-0116).
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertInDiscard(gain);
		assertEquals(0, scn.GetWoundsOn(troop));
		assertInZone(Zone.SUPPORT, teeth);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
