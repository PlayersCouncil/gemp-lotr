package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_10_070_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nelya", "10_70");

					put("lemenya", "1_232");        // Úlairë Lemenya, Lieutenant of Morgul: a Nazgûl, vitality 2; her text needs a [wraith] condition on a companion
					put("otsea", "1_235");          // Úlairë Otsëa, Lieutenant of Morgul: a Nazgûl, vitality 3; his Shadow ability is used only on purpose

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
	public void UlaireNelyaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Úlairë Nelya, Thrall of The One
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 2
		 * Game Text: <b>Enduring</b>. <b>Fierce</b>.<br>Shadow cards cannot exert Úlairë Nelya during a skirmish phase.<br><b>Skirmish:</b> Heal Úlairë Nelya to add (1) for each burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("nelya");

		assertEquals("Úlairë Nelya", card.getBlueprint().getTitle());
		assertEquals("Thrall of The One", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ENDURING));
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(2, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ShadowCardsExertNelyaInTheManeuverAndRegroupPhasesAndNotInTheSkirmishPhaseBetween() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var nelya = scn.GetShadowCard("nelya");
		var charge = scn.GetShadowCard("charge");
		var darkness = scn.GetShadowCard("darkness");
		var winged = scn.GetShadowCard("winged");

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);
		scn.MoveCardsToSupportArea(winged);
		scn.MoveCardsToHand(charge, darkness);
		scn.SetTwilight(10);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Maneuver: Nelya is the only Nazgûl, so she is the one who pays for Relentless Charge.
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertTrue(scn.ShadowPlayAvailable(charge));
		scn.ShadowPlayCard(charge);
		assertEquals(1, scn.GetWoundsOn(nelya));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, nelya);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Skirmish: 10 set + 2 companions + 2 for the site, and Relentless Charge cost nothing. Nelya, vitality 3
		// with 1 wound, could exert, but a Shadow card cannot exert her in a skirmish phase.
		assertEquals(14, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(nelya));
		assertFalse(scn.ShadowPlayAvailable(darkness));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Nelya, 10 + 2 for her wound, beats Aragorn's 8 without killing him; the fierce assignment is passed over.
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// Regroup: she pays for Winged and Ominous.
		scn.FreepsPass();
		assertEquals(0, scn.GetCultureTokensOn(winged));
		assertTrue(scn.ShadowActionAvailable(winged));
		scn.ShadowUseCardAction(winged);
		assertEquals(2, scn.GetWoundsOn(nelya));
		assertEquals(1, scn.GetCultureTokensOn(winged));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ShadowEventExertsAnotherNazgulAndNotNelyaWhileAnotherMinionSkirmishes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var rider = scn.GetFreepsCard("rider");
		scn.MoveCompanionsToTable(aragorn, rider);

		var nelya = scn.GetShadowCard("nelya");
		var lemenya = scn.GetShadowCard("lemenya");
		var otsea = scn.GetShadowCard("otsea");
		var darkness = scn.GetShadowCard("darkness");

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, lemenya, otsea);
		scn.MoveCardsToHand(darkness);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, lemenya}, new PhysicalCardImpl[]{rider, nelya});
		// Úlairë Otsëa is left out of the fighting.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(3);
		scn.FreepsPass();

		// It is a skirmish phase, though the skirmish is not Nelya's: her own has not begun.
		assertTrue(scn.IsCharSkirmishing(lemenya));
		assertFalse(scn.IsCharSkirmishing(nelya));
		assertTrue(scn.IsCharAssigned(nelya));
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(otsea));
		assertTrue(scn.ShadowPlayAvailable(darkness));

		scn.ShadowPlayCard(darkness);
		// The event may exert either of the other two Nazgûl, fighting or not, and may not exert Nelya.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, otsea));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(nelya));

		scn.ShadowChooseCard(otsea);
		assertEquals(1, scn.GetWoundsOn(otsea));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AFreePeoplesCardAndASiteExertNelyaInHerSkirmishAndAShadowEventCannot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rider = scn.GetFreepsCard("rider");
		var mount = scn.GetFreepsCard("mount");
		scn.MoveCompanionsToTable(rider);
		scn.AttachCardsTo(rider, mount);

		var nelya = scn.GetShadowCard("nelya");
		var darkness = scn.GetShadowCard("darkness");

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);
		scn.MoveCardsToHand(darkness);

		scn.SkipToAssignments();
		assertEquals(2, scn.GetCurrentSiteNumber());
		var ettenmoors = scn.GetCurrentSite();
		scn.FreepsAssignToMinions(rider, nelya);

		assertEquals(0, scn.GetWoundsOn(nelya));
		scn.FreepsResolveSkirmish(rider);
		// Rider's Mount is a Free Peoples card: at the start of the skirmish it exerts Nelya all the same.
		assertTrue(scn.IsCharSkirmishing(nelya));
		assertEquals(1, scn.GetWoundsOn(nelya));

		scn.SetTwilight(3);
		scn.FreepsPass();
		// The event's 3 are in the pool and Nelya, vitality 3 with 1 wound, has an exertion left in her: she is
		// the only Nazgûl, and a Shadow card cannot exert her.
		assertEquals(3, scn.GetTwilight());
		assertFalse(scn.ShadowPlayAvailable(darkness));

		// Ettenmoors is a site and no Shadow card: its ability exerts her.
		assertTrue(scn.ShadowActionAvailable(ettenmoors));
		scn.ShadowUseCardAction(ettenmoors);
		assertEquals(2, scn.GetWoundsOn(nelya));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityHealsNelyaToAddOneForEachBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var nelya = scn.GetShadowCard("nelya");
		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, lemenya);
		scn.AddBurdens(2);
		scn.AddWoundsToChar(nelya, 2);
		// Another wounded Nazgûl, who is not "Nelya".
		scn.AddWoundsToChar(lemenya, 1);
		assertEquals(2, scn.GetBurdens());
		assertEquals(2, scn.GetWoundsOn(nelya));
		assertEquals(1, scn.GetWoundsOn(lemenya));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, nelya);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(0);
		scn.FreepsPass();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(nelya));
		scn.ShadowUseCardAction(nelya);
		// 2 burdens: (2).
		assertEquals(1, scn.GetWoundsOn(nelya));
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(2, scn.GetTwilight());
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.AddBurdens(1);
		assertEquals(3, scn.GetBurdens());
		scn.FreepsPass();
		scn.ShadowUseCardAction(nelya);
		// 3 burdens: 2 + 3.
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertEquals(5, scn.GetTwilight());
		assertEquals(3, scn.GetBurdens());

		// No wound is left on her to heal: the ability cannot be paid for again.
		scn.FreepsPass();
		assertFalse(scn.ShadowActionAvailable(nelya));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
