package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_081_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gates", "3_81");

					put("lemenya", "1_232");    // Úlairë Lemenya: [wraith] Nazgûl, cost 4, vitality 2; weakens only bearers of a [wraith] condition
					put("khamul", "103_61");    // Úlairë Attëa, Khamûl of the East: a [raider] Nazgûl; his bonuses need 3 burdens
					put("nelya", "2_84");       // Úlairë Nelya: twilight [wraith] Nazgûl; fierce and damage +1 only at 2 burdens or 2 wounds on the Ring-bearer
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc and no Nazgûl, strength 5; stronger only at 2 wounds on the Ring-bearer

					// [wraith] cards that add a burden: an event, a minion, an item
					put("found", "2_78");       // It Wants to be Found: Maneuver: Spot a twilight Nazgûl to add a burden.
					put("world", "1_228");      // The Twilight World: Response: If the Ring-bearer puts on The One Ring, exert a Nazgûl to add 2 burdens.
					put("brute", "7_188");      // Morgul Brute: when played, may spot a Nazgûl to add a burden; the Free Peoples player may wound the Ring-bearer to prevent it
					put("witchking", "12_183"); // The Witch-king, Black Lord: strength 14, fierce; his other texts are about playing him
					put("blade", "11_214");     // The Pale Blade: on The Witch-king, each time he wins a skirmish, may exert him to add a burden

					// A Shadow card of another culture that adds a burden
					put("stormer", "8_101");    // Gorgoroth Stormer: [sauron] Orc; Shadow: if you have initiative, exert it to add a burden and make the Free Peoples player draw

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion, vitality 4
					put("gimli", "1_13");       // Gimli, Son of Glóin: a [dwarven] Dwarf companion, vitality 3; no archer, a skirmish ability used only on purpose
					put("orophin", "1_56");     // Orophin, Lórien Bowman: an Elf ally, vitality 3; an archery ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GatesoftheDeadCityStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Gates of the Dead City
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot a Nazgûl. Plays to your support area.
		 * 	Each time a [wraith] card adds a burden, the Free Peoples player must exert a companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gates");

		assertEquals("Gates of the Dead City", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilANazgulIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gates = scn.GetShadowCard("gates");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToHand(gates, lemenya);
		assertInHand(gates, lemenya);

		scn.StartGame();
		// A [wraith] minion who is no Nazgûl.
		scn.MoveMinionsToTable(hound);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.GetTwilight() >= 1);
		assertFalse(scn.ShadowPlayAvailable(gates));

		scn.ShadowPlayCard(lemenya);
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya);
		assertTrue(scn.ShadowPlayAvailable(gates));

		scn.ShadowPlayCard(gates);
		assertInZone(Zone.SUPPORT, gates);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CanBePlayedSpottingANazgulWhoIsNotWraith() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gates = scn.GetShadowCard("gates");
		var khamul = scn.GetShadowCard("khamul");
		scn.MoveCardsToHand(gates);
		assertInHand(gates);

		scn.StartGame();
		// The [raider] Nazgûl is the only Nazgûl.
		scn.MoveMinionsToTable(khamul);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(gates));
		scn.ShadowPlayCard(gates);
		assertInZone(Zone.SUPPORT, gates);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AWraithEventAddingABurdenMakesTheFreePeoplesPlayerExertACompanionOfHisChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gates = scn.GetShadowCard("gates");
		var found = scn.GetShadowCard("found");
		var nelya = scn.GetShadowCard("nelya");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToSupportArea(gates);
		scn.MoveCardsToHand(found);
		assertInZone(Zone.SUPPORT, gates);
		assertInHand(found);

		scn.StartGame();
		// Gimli, vitality 3 with 2 wounds, is exhausted.
		scn.AddWoundsToChar(gimli, 2);
		scn.MoveMinionsToTable(nelya);
		assertEquals(2, scn.GetWoundsOn(gimli));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(orophin));
		scn.ShadowPlayCard(found);
		assertEquals(1, scn.GetBurdens());

		// The Free Peoples player chooses: a companion of either kind, not the exhausted one, the ally nor a minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, orophin, nelya));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(orophin));
		assertInDiscard(found);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AWraithMinionAddingABurdenMakesTheFreePeoplesPlayerExertACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gates = scn.GetShadowCard("gates");
		var brute = scn.GetShadowCard("brute");
		var lemenya = scn.GetShadowCard("lemenya");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(gates);
		scn.MoveCardsToHand(brute);
		assertInZone(Zone.SUPPORT, gates);
		assertInHand(brute);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		scn.ShadowPlayCard(brute);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(brute));
		scn.ShadowChooseOptionalTrigger(brute);
		// The Free Peoples player lets the burden be added.
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseNo();
		assertEquals(1, scn.GetBurdens());

		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsChooseCard(frodo);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AWraithItemAddingABurdenMakesTheFreePeoplesPlayerExertACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gates = scn.GetShadowCard("gates");
		var witchking = scn.GetShadowCard("witchking");
		var blade = scn.GetShadowCard("blade");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToSupportArea(gates);
		assertInZone(Zone.SUPPORT, gates);

		scn.StartGame();
		scn.MoveMinionsToTable(witchking);
		scn.AttachCardsTo(witchking, blade);
		assertAttachedTo(blade, witchking);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, witchking);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// The Witch-king (14, and 3 for the Blade) has won and Aragorn (8) is overwhelmed: the Blade's trigger.
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(witchking));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(blade));
		scn.ShadowChooseOptionalTrigger(blade);
		assertEquals(1, scn.GetWoundsOn(witchking));
		assertEquals(1, scn.GetBurdens());

		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.FreepsChooseCard(gimli);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsDecisionAvailable("Assign"));
	}

	@Test
	public void ABurdenAddedByAShadowCardOfAnotherCultureExertsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gates = scn.GetShadowCard("gates");
		var stormer = scn.GetShadowCard("stormer");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(gates);
		assertInZone(Zone.SUPPORT, gates);

		scn.StartGame();
		scn.MoveMinionsToTable(stormer);
		scn.FreepsPass();

		// The Free Peoples hand is empty, so the Shadow player has initiative.
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(stormer));
		scn.ShadowUseCardAction(stormer);
		assertEquals(1, scn.GetWoundsOn(stormer));
		assertEquals(1, scn.GetBurdens());

		// Nobody is asked to exert, and no companion has.
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TwoBurdensAddedAtOnceByAWraithCardExertTwiceAndTheRingsOwnBurdenNotAtAll() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gates = scn.GetShadowCard("gates");
		var world = scn.GetShadowCard("world");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToSupportArea(gates);
		scn.MoveCardsToHand(world);
		assertInZone(Zone.SUPPORT, gates);
		assertInHand(world);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, hound);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, hound);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(frodo);
		scn.PassSkirmishActions();

		// The Hound (5) beats Frodo (3, and 1 for the Ring): the Ring's response puts it on.
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ring));
		scn.FreepsChooseOptionalTrigger(ring);
		assertTrue(scn.RBWearingOneRing());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(world));
		scn.ShadowChooseOptionalTrigger(world);
		assertEquals(1, scn.GetWoundsOn(lemenya));

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		// One exertion for each of the two burdens.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, gimli));
		scn.FreepsChooseCard(aragorn);
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, gimli));
		scn.FreepsChooseCard(gimli);

		// 2 from the event, and 1 for the wound the Ring turns into a burden, which asks for no exertion.
		assertEquals(3, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInDiscard(world);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
