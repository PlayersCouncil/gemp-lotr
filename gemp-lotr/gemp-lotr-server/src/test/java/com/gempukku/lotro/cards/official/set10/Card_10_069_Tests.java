package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_10_069_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lemenya", "10_69");

					put("nelya", "1_233");          // Úlairë Nelya, Lieutenant of Morgul: a Nazgûl, vitality 3; her Shadow ability is used only on purpose
					put("otsea", "1_235");          // Úlairë Otsëa, Lieutenant of Morgul: a Nazgûl, vitality 3; his Shadow ability is used only on purpose
					put("hound", "7_192");          // Morgul Hound: a [wraith] Orc, vitality 2; strength +4 and fierce only at 2 wounds on the Ring-bearer
					put("mongrel", "7_194");        // Morgul Mongrel: a [wraith] Orc, vitality 2; fierce and damage +1 only at 3 wounds on the Ring-bearer
					put("chieftain", "1_266");      // Orc Chieftain: a [sauron] Orc, vitality 2; his text answers a weapon played on him

					put("darkness", "8_68");        // Beyond All Darkness: Shadow Skirmish event, cost 3, "Exert a Nazgûl to make him strength +2 and damage +1"
					put("charge", "1_223");         // Relentless Charge: Shadow event, cost 0, "Maneuver: Exert a Nazgûl to wound every archer companion"
					put("winged", "6_89");          // Winged and Ominous: Shadow condition, "Regroup: Exert a Nazgûl to place a [wraith] token on this card"

					put("aragorn", "1_89");         // Aragorn, Ranger of the North: strength 8, vitality 4; no archer
					put("rider", "4_286");          // Rider of Rohan: strength 5, vitality 3; his ability needs a minion skirmishing an unbound companion and is used only on purpose
					put("mount", "4_287");          // Rider's Mount: Free Peoples possession, "At the start of each skirmish involving bearer, each minion skirmishing bearer must exert"
				}},
				// Site 2 is Ettenmoors: "Skirmish: Exert your companion or minion to make that character strength +2."
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireLemenyaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Úlairë Lemenya, Thrall of The One
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: <b>Enduring</b>. <b>Fierce</b>.<br>Shadow cards cannot exert Úlairë Lemenya during a skirmish phase.<br><b>Skirmish:</b> Heal Úlairë Lemenya to wound up to 3 other [wraith] minions.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("lemenya");

		assertEquals("Úlairë Lemenya", card.getBlueprint().getTitle());
		assertEquals("Thrall of The One", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ENDURING));
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ShadowCardsExertLemenyaInTheManeuverAndRegroupPhasesAndNotInTheSkirmishPhaseBetween() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var lemenya = scn.GetShadowCard("lemenya");
		var charge = scn.GetShadowCard("charge");
		var darkness = scn.GetShadowCard("darkness");
		var winged = scn.GetShadowCard("winged");

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.MoveCardsToSupportArea(winged);
		scn.MoveCardsToHand(charge, darkness);
		scn.SetTwilight(10);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Maneuver: Lemenya is the only Nazgûl, so he is the one who pays for Relentless Charge.
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertTrue(scn.ShadowPlayAvailable(charge));
		scn.ShadowPlayCard(charge);
		assertEquals(1, scn.GetWoundsOn(lemenya));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lemenya);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Skirmish: his own ability heals him, so that he has an exertion in him again.
		assertTrue(scn.ShadowActionAvailable(lemenya));
		scn.ShadowUseCardAction(lemenya);
		assertEquals(0, scn.GetWoundsOn(lemenya));
		scn.FreepsPass();

		// 10 set + 2 companions + 2 for the site, and Relentless Charge cost nothing: Beyond All Darkness can be
		// paid for, and Lemenya could exert, but a Shadow card cannot exert him in a skirmish phase.
		assertEquals(14, scn.GetTwilight());
		assertFalse(scn.ShadowPlayAvailable(darkness));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Lemenya's 9 beats Aragorn's 8; the fierce assignment is passed over.
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// Regroup: he pays for Winged and Ominous.
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetCultureTokensOn(winged));
		assertTrue(scn.ShadowActionAvailable(winged));
		scn.ShadowUseCardAction(winged);
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(1, scn.GetCultureTokensOn(winged));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ShadowEventExertsAnotherNazgulAndNotLemenyaWhileAnotherMinionSkirmishes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var rider = scn.GetFreepsCard("rider");
		scn.MoveCompanionsToTable(aragorn, rider);

		var lemenya = scn.GetShadowCard("lemenya");
		var nelya = scn.GetShadowCard("nelya");
		var otsea = scn.GetShadowCard("otsea");
		var darkness = scn.GetShadowCard("darkness");

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, nelya, otsea);
		scn.MoveCardsToHand(darkness);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, nelya}, new PhysicalCardImpl[]{rider, lemenya});
		// Úlairë Otsëa is left out of the fighting.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(3);
		scn.FreepsPass();

		// It is a skirmish phase, though the skirmish is not Lemenya's: his own has not begun.
		assertTrue(scn.IsCharSkirmishing(nelya));
		assertFalse(scn.IsCharSkirmishing(lemenya));
		assertTrue(scn.IsCharAssigned(lemenya));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertEquals(0, scn.GetWoundsOn(otsea));
		assertTrue(scn.ShadowPlayAvailable(darkness));

		scn.ShadowPlayCard(darkness);
		// The event may exert either of the other two Nazgûl, fighting or not, and may not exert Lemenya.
		assertTrue(scn.ShadowHasCardChoiceAvailable(nelya, otsea));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(lemenya));

		scn.ShadowChooseCard(otsea);
		assertEquals(1, scn.GetWoundsOn(otsea));
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AFreePeoplesCardAndASiteExertLemenyaInHisSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rider = scn.GetFreepsCard("rider");
		var mount = scn.GetFreepsCard("mount");
		scn.MoveCompanionsToTable(rider);
		scn.AttachCardsTo(rider, mount);

		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);

		scn.SkipToAssignments();
		assertEquals(2, scn.GetCurrentSiteNumber());
		var ettenmoors = scn.GetCurrentSite();
		scn.FreepsAssignToMinions(rider, lemenya);

		assertEquals(0, scn.GetWoundsOn(lemenya));
		scn.FreepsResolveSkirmish(rider);
		// Rider's Mount is a Free Peoples card: at the start of the skirmish it exerts Lemenya all the same.
		assertTrue(scn.IsCharSkirmishing(lemenya));
		assertEquals(1, scn.GetWoundsOn(lemenya));

		// His own ability heals him; then Ettenmoors, a site and no Shadow card, exerts him.
		scn.FreepsPass();
		scn.ShadowUseCardAction(lemenya);
		assertEquals(0, scn.GetWoundsOn(lemenya));
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(ettenmoors));
		scn.ShadowUseCardAction(ettenmoors);
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityHealsLemenyaToWoundUpToThreeOtherWraithMinions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var rider = scn.GetFreepsCard("rider");
		scn.MoveCompanionsToTable(aragorn, rider);
		var frodo = scn.GetRingBearer();

		var lemenya = scn.GetShadowCard("lemenya");
		var nelya = scn.GetShadowCard("nelya");
		var otsea = scn.GetShadowCard("otsea");
		var hound = scn.GetShadowCard("hound");
		var mongrel = scn.GetShadowCard("mongrel");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, nelya, otsea, hound, mongrel, chieftain);
		scn.AddWoundsToChar(lemenya, 1);
		// Vitality 2: the Hound is exhausted, so a wound kills it.
		scn.AddWoundsToChar(hound, 1);
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(1, scn.GetWoundsOn(hound));

		scn.SkipToAssignments();
		// Nelya shares Lemenya's skirmish, Otsëa is assigned to a skirmish that has not begun, and the Hound and
		// the Mongrel are not assigned at all: the text names no skirmish for the minions it wounds.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, lemenya}, new PhysicalCardImpl[]{rider, otsea});
		scn.ShadowAssignToMinions(aragorn, nelya);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.IsCharSkirmishing(nelya));
		assertFalse(scn.IsCharSkirmishing(otsea));
		assertTrue(scn.IsCharAssigned(otsea));
		assertFalse(scn.IsCharAssigned(hound));
		assertFalse(scn.IsCharAssigned(mongrel));

		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertEquals(0, scn.GetWoundsOn(otsea));
		assertEquals(1, scn.GetWoundsOn(hound));
		assertEquals(0, scn.GetWoundsOn(mongrel));
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowActionAvailable(lemenya));

		scn.ShadowUseCardAction(lemenya);
		assertEquals(0, scn.GetWoundsOn(lemenya));
		// Two Nazgûl and two [wraith] Orcs; not Lemenya himself, not the [sauron] Orc, no companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(nelya, otsea, hound, mongrel));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(lemenya, chieftain, aragorn, rider, frodo));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCards(nelya, hound, mongrel);
		assertEquals(1, scn.GetWoundsOn(nelya));
		assertEquals(1, scn.GetWoundsOn(mongrel));
		assertInZone(Zone.DISCARD, hound);
		assertEquals(0, scn.GetWoundsOn(otsea));
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// No wound is left on Lemenya to heal: the ability cannot be paid for again.
		scn.FreepsPass();
		assertFalse(scn.ShadowActionAvailable(lemenya));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
