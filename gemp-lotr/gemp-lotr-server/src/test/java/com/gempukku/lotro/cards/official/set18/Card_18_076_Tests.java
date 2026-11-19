package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_076_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("treachery", "18_76");
					// Gríma, Witless Worm: the [men] Gríma, a Man minion; three regroup abilities, never used.
					put("grima", "18_67");
					// Gríma, Son of Galmód: the [isengard] Gríma; a strength penalty only on companions bearing a
					// Shadow condition, and none bears one.
					put("isengardgrima", "4_153");
					// Warrior of Dunland: a [men] Man minion who is no Gríma; ambush.
					put("warrior", "11_103");
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc, strength 13, no text
					// Morgul Slayer: a [sauron] Orc, strength 7, vitality 2.  "Regroup: Exert this minion to wound a
					// companion (except the Ring-bearer)."
					put("slayer", "3_93");

					// Aragorn, Ranger of the North: an unbound [gondor] Man; a maneuver ability never used.
					put("aragorn", "1_89");
					// Huorn: an unbound [gandalf] Tree, strength 6, vitality 4.  "Damage +1.  This companion is
					// strength +1 for each Ent you can spot.  Response: If this companion is about to take a wound,
					// exert an Ent to prevent that."
					put("huorn1", "9_25");
					put("huorn2", "9_25");
					// Ent Avenger: an unbound [gandalf] Ent, vitality 4; a strength bonus only while 3 Ents can be
					// spotted.  The Ent the Huorns count and exert.
					put("avenger", "6_27");
					// Sam, Son of Hamfast: Ring-bound and not the Ring-bearer; a fellowship ability never used.
					put("sam", "1_311");
					// Bounder: a Hobbit ally whose home is site 2, where these Shadow phases are; a skirmish ability
					// never used.
					put("bounder", "1_286");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TreacheryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Treachery
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: Spot Gríma to exert an unbound companion. That companion cannot use his or her game text until the regroup phase.
		*/

		var scn = GetScenario();

		var treachery = scn.GetFreepsCard("treachery");

		assertEquals("Treachery", treachery.getBlueprint().getTitle());
		assertNull(treachery.getBlueprint().getSubtitle());
		assertFalse(treachery.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, treachery.getBlueprint().getSide());
		assertEquals(Culture.MEN, treachery.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, treachery.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(treachery, Timeword.SHADOW));
		assertEquals(3, treachery.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithGrimaSpottedItExertsAnUnboundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var huorn = scn.GetFreepsCard("huorn1");
		var avenger = scn.GetFreepsCard("avenger");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var treachery = scn.GetShadowCard("treachery");
		var grima = scn.GetShadowCard("grima");
		scn.MoveCompanionsToTable(aragorn, huorn, avenger, sam);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(treachery);

		scn.StartGame();
		// Vitality 4 with 3 wounds: the Ent Avenger cannot exert.
		scn.AddWoundsToChar(avenger, 3);
		assertEquals(3, scn.GetWoundsOn(avenger));
		assertInZone(Zone.HAND, treachery);
		scn.FreepsPass();
		scn.MoveMinionsToTable(grima);
		scn.ShadowRefreshDecision();

		assertTrue(scn.ShadowPlayAvailable(treachery));
		scn.ShadowPlayCard(treachery);

		// Two unbound companions of other cultures and races.  Not the exhausted one, not Sam (Ring-bound), not
		// Frodo, and not the ally.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, huorn));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(avenger, sam, frodo, bounder));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(huorn));
		scn.ShadowChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(huorn));
		assertEquals(3, scn.GetWoundsOn(avenger));
		assertInZone(Zone.DISCARD, treachery);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWithAMenManWhoIsNoGrima() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var treachery = scn.GetShadowCard("treachery");
		var warrior = scn.GetShadowCard("warrior");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(treachery);

		scn.StartGame();
		assertInZone(Zone.HAND, treachery);
		// 10 set + 2 for Ettenmoors + 2 for the companions: enough for the event's 3.
		scn.SetTwilight(10);
		scn.FreepsPass();
		scn.MoveMinionsToTable(warrior);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(treachery));
		assertEquals(0, scn.GetWoundsOn(aragorn));
	}

	@Test
	public void WithTheIsengardGrimaAndNoUnboundCompanionItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var treachery = scn.GetShadowCard("treachery");
		var grima = scn.GetShadowCard("isengardgrima");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(treachery);

		scn.StartGame();
		assertInZone(Zone.HAND, treachery);
		scn.SetTwilight(10);
		scn.FreepsPass();
		scn.MoveMinionsToTable(grima);
		scn.ShadowRefreshDecision();

		// The target is no requirement: the event is played, and nobody is there to exert.
		assertTrue(scn.ShadowPlayAvailable(treachery));
		scn.ShadowPlayCard(treachery);

		assertInZone(Zone.DISCARD, treachery);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheCompanionExertedHasNoGameTextUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var huorn1 = scn.GetFreepsCard("huorn1");
		var huorn2 = scn.GetFreepsCard("huorn2");
		var avenger = scn.GetFreepsCard("avenger");
		var treachery = scn.GetShadowCard("treachery");
		var grima = scn.GetShadowCard("grima");
		var troop = scn.GetShadowCard("troop");
		var slayer = scn.GetShadowCard("slayer");
		scn.MoveCompanionsToTable(huorn1, huorn2, avenger);
		scn.MoveCardsToHand(treachery);

		scn.StartGame();
		assertInZone(Zone.HAND, treachery);
		scn.SetTwilight(10);
		scn.FreepsPass();
		scn.MoveMinionsToTable(grima, troop, slayer);
		scn.ShadowRefreshDecision();

		// Each Huorn: 6, +1 for the Ent Avenger, and damage +1.
		assertEquals(7, scn.GetStrength(huorn1));
		assertEquals(1, scn.GetKeywordCount(huorn1, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(huorn1));
		scn.ShadowPlayCard(treachery);
		assertTrue(scn.ShadowHasCardChoiceAvailable(huorn1, huorn2, avenger));
		scn.ShadowChooseCard(huorn1);
		assertEquals(1, scn.GetWoundsOn(huorn1));
		assertEquals(6, scn.GetStrength(huorn1));
		assertEquals(0, scn.GetKeywordCount(huorn1, Keyword.DAMAGE));

		// The other Huorn (7) loses to the Troop (13) and is not overwhelmed; the Huorn exerted (6) loses to the
		// Slayer (7), who lives on into the regroup phase.  Gríma and the Ent Avenger are left out.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{huorn2, troop}, new PhysicalCardImpl[]{huorn1, slayer});
		scn.ShadowDeclineAssignments();

		// The Huorn not chosen keeps his text: his Response as he is about to take the wound for losing.
		scn.FreepsResolveSkirmish(huorn2);
		assertEquals(7, scn.GetStrength(huorn2));
		assertEquals(1, scn.GetKeywordCount(huorn2, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(huorn2));
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(huorn2));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(huorn2));

		// In the next skirmish the Huorn exerted still has none: no bonus, no damage +1, and no Response.
		scn.FreepsResolveSkirmish(huorn1);
		assertEquals(6, scn.GetStrength(huorn1));
		assertEquals(0, scn.GetKeywordCount(huorn1, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(avenger));
		scn.PassSkirmishActions();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(huorn1));
		assertEquals(2, scn.GetWoundsOn(huorn1));
		assertEquals(0, scn.GetWoundsOn(avenger));

		// In the regroup phase his text is back: the Slayer's wound on him is answered by his Response, and the
		// Ent exerts for it.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(7, scn.GetStrength(huorn1));
		assertEquals(1, scn.GetKeywordCount(huorn1, Keyword.DAMAGE));
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(slayer));
		scn.ShadowUseCardAction(slayer);
		assertEquals(1, scn.GetWoundsOn(slayer));
		scn.ShadowChooseCard(huorn1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(huorn1));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(2, scn.GetWoundsOn(huorn1));
		assertEquals(1, scn.GetWoundsOn(avenger));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
