package com.gempukku.lotro.cards.unofficial.pc.errata.set10;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_10_068_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("enquea", "60_68");

					put("lemenya", "1_232");        // Úlairë Lemenya, Lieutenant of Morgul: a Nazgûl, strength 9, vitality 2; his text needs a [wraith] condition on a companion
					put("nelya", "2_84");           // Úlairë Nelya, Ringwraith in Twilight: a Nazgûl, strength 10, vitality 3; nothing else below 2 burdens
					put("darkness", "8_68");        // Beyond All Darkness: Shadow Skirmish event, cost 3, "Exert a Nazgûl to make him strength +2 and damage +1"
					put("charge", "1_223");         // Relentless Charge: Shadow event, cost 0, "Maneuver: Exert a Nazgûl to wound every archer companion"
					put("winged", "6_89");          // Winged and Ominous: Shadow condition, "Regroup: Exert a Nazgûl to place a [wraith] token on this card"
					put("shagrat", "10_99");        // Shagrat, Captain of Cirith Ungol: a Shadow character; "Each time Shagrat loses a skirmish, you may exert a character"

					put("aragorn", "1_89");         // Aragorn, Ranger of the North: strength 8, vitality 4; no archer
					put("boromir", "1_97");         // Boromir, Son of Denethor: a skirmish ability used only on purpose
					put("rider", "4_286");          // Rider of Rohan: strength 5, vitality 3; his ability needs a mount
					put("mount", "4_287");          // Rider's Mount: Free Peoples possession, "At the start of each skirmish involving bearer, each minion skirmishing bearer must exert"
				}},
				// Site 2 is Ettenmoors: "Skirmish: Exert your companion or minion to make that character strength +2."
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireEnqueaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Ulaire Enquea, Thrall of The One
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Nazgul
		 * Strength: 11
		 * Vitality: 4
		 * Site Number: 3
		 * Game Text: <b>Enduring.</b>
		 * Shadow cards cannot exert Ulaire Enquea during a skirmish phase.
		 * <b>Skirmish:</b> If Ulaire Enquea is skirmishing, remove (1) and heal him to add a burden.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("enquea");

		assertEquals("Ulaire Enquea", card.getBlueprint().getTitle());
		assertEquals("Thrall of The One", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ENDURING));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ShadowEventExertsAnotherNazgulAndNotEnqueaWhileAnotherMinionSkirmishes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var rider = scn.GetFreepsCard("rider");
		scn.MoveCompanionsToTable(aragorn, rider);

		var enquea = scn.GetShadowCard("enquea");
		var lemenya = scn.GetShadowCard("lemenya");
		var nelya = scn.GetShadowCard("nelya");
		var darkness = scn.GetShadowCard("darkness");

		scn.StartGame();
		scn.MoveMinionsToTable(enquea, lemenya, nelya);
		scn.MoveCardsToHand(darkness);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, lemenya}, new PhysicalCardImpl[]{rider, enquea});
		// Úlairë Nelya is left out of the fighting.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(3);
		scn.FreepsPass();

		// It is a skirmish phase, though the skirmish is not Enquea's: his own has not begun.
		assertTrue(scn.IsCharSkirmishing(lemenya));
		assertFalse(scn.IsCharSkirmishing(enquea));
		assertTrue(scn.IsCharAssigned(enquea));
		assertEquals(0, scn.GetWoundsOn(enquea));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertTrue(scn.ShadowPlayAvailable(darkness));

		scn.ShadowPlayCard(darkness);
		// The event may exert either of the other two Nazgûl, fighting or not, and may not exert Enquea.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, nelya));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(enquea));

		scn.ShadowChooseCard(nelya);
		assertEquals(1, scn.GetWoundsOn(nelya));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(enquea));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AFreePeoplesCardExertsEnqueaInHisSkirmishAndAShadowEventCannot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rider = scn.GetFreepsCard("rider");
		var mount = scn.GetFreepsCard("mount");
		scn.MoveCompanionsToTable(rider);
		scn.AttachCardsTo(rider, mount);

		var enquea = scn.GetShadowCard("enquea");
		var darkness = scn.GetShadowCard("darkness");

		scn.StartGame();
		scn.MoveMinionsToTable(enquea);
		scn.MoveCardsToHand(darkness);
		scn.AddWoundsToChar(enquea, 1);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(rider, enquea);

		assertEquals(1, scn.GetWoundsOn(enquea));
		scn.FreepsResolveSkirmish(rider);
		// Rider's Mount is a Free Peoples card: at the start of the skirmish it exerts Enquea all the same.
		assertTrue(scn.IsCharSkirmishing(enquea));
		assertEquals(2, scn.GetWoundsOn(enquea));

		scn.SetTwilight(3);
		scn.FreepsPass();
		// The event's 3 are in the pool and Enquea, vitality 4 with 2 wounds, has an exertion left in him:
		// he is the only Nazgûl, and a Shadow card cannot exert him.
		assertEquals(3, scn.GetTwilight());
		assertFalse(scn.ShadowPlayAvailable(darkness));
		assertEquals(2, scn.GetWoundsOn(enquea));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ShadowCardsExertEnqueaInTheManeuverAndRegroupPhasesAndNotInTheSkirmishPhaseBetween() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var enquea = scn.GetShadowCard("enquea");
		var charge = scn.GetShadowCard("charge");
		var darkness = scn.GetShadowCard("darkness");
		var winged = scn.GetShadowCard("winged");

		scn.StartGame();
		scn.MoveMinionsToTable(enquea);
		scn.MoveCardsToSupportArea(winged);
		scn.MoveCardsToHand(charge, darkness);
		scn.SetTwilight(10);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Maneuver: Enquea is the only Nazgûl, so he is the one who pays for Relentless Charge.
		assertEquals(0, scn.GetWoundsOn(enquea));
		assertTrue(scn.ShadowPlayAvailable(charge));
		scn.ShadowPlayCard(charge);
		assertEquals(1, scn.GetWoundsOn(enquea));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, enquea);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Skirmish: 10 set + 2 companions + 2 for the site, and Relentless Charge cost nothing.
		assertEquals(14, scn.GetTwilight());
		assertFalse(scn.ShadowPlayAvailable(darkness));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Enquea, 11 + 2 for his wound, beats Aragorn's 8 without killing him.
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(enquea));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// Regroup: he pays for Winged and Ominous.
		scn.FreepsPass();
		assertEquals(0, scn.GetCultureTokensOn(winged));
		assertTrue(scn.ShadowActionAvailable(winged));
		scn.ShadowUseCardAction(winged);
		assertEquals(2, scn.GetWoundsOn(enquea));
		assertEquals(1, scn.GetCultureTokensOn(winged));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AShadowCharactersEffectInTheSkirmishPhaseMayExertAnotherCharacterButNotEnquea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var enquea = scn.GetShadowCard("enquea");
		var lemenya = scn.GetShadowCard("lemenya");
		var shagrat = scn.GetShadowCard("shagrat");

		scn.StartGame();
		scn.MoveMinionsToTable(enquea, lemenya, shagrat);
		// Strength that only arranges the board: Aragorn beats Shagrat's 13.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 10));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, shagrat}, new PhysicalCardImpl[]{boromir, enquea});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		// Shagrat has lost: his "may exert a character" is offered while the skirmish phase is resolved.
		assertEquals(1, scn.GetWoundsOn(shagrat));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(shagrat));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(enquea));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, boromir, lemenya));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(enquea));
		scn.ShadowChooseCard(lemenya);

		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(enquea));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void SkirmishAbilityRemovesOneTwilightAndHealsEnqueaToAddABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();

		var enquea = scn.GetShadowCard("enquea");
		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		scn.MoveMinionsToTable(enquea, lemenya);
		scn.AddBurdens(1);
		scn.AddWoundsToChar(enquea, 2);
		// Another wounded Nazgûl, who is not "him".
		scn.AddWoundsToChar(lemenya, 1);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, enquea);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(frodo);
		scn.SetTwilight(1);
		scn.FreepsPass();

		assertTrue(scn.IsCharSkirmishing(enquea));
		assertEquals(2, scn.GetWoundsOn(enquea));
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(1, scn.GetTwilight());
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.ShadowActionAvailable(enquea));

		scn.ShadowUseCardAction(enquea);
		assertEquals(1, scn.GetWoundsOn(enquea));
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetTwilight());
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// A wound is still on him, but the pool is empty: the price cannot be paid again.
		scn.FreepsPass();
		assertFalse(scn.ShadowActionAvailable(enquea));
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoWoundToHealTheAbilityIsNotOfferedUntilTheSiteHasExertedEnquea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var enquea = scn.GetShadowCard("enquea");
		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		scn.MoveMinionsToTable(enquea, lemenya);

		scn.SkipToAssignments();
		assertEquals(2, scn.GetCurrentSiteNumber());
		var ettenmoors = scn.GetCurrentSite();
		scn.FreepsAssignToMinions(aragorn, enquea);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(3);
		scn.FreepsPass();

		// Twilight to spare, and nothing on Enquea to heal.
		assertTrue(scn.IsCharSkirmishing(enquea));
		assertEquals(0, scn.GetWoundsOn(enquea));
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.ShadowActionAvailable(enquea));

		// Ettenmoors is a site and no Shadow card: its ability may exert him in a skirmish phase.
		assertTrue(scn.ShadowActionAvailable(ettenmoors));
		scn.ShadowUseCardAction(ettenmoors);
		assertTrue(scn.ShadowHasCardChoiceAvailable(enquea, lemenya));
		scn.ShadowChooseCard(enquea);
		assertEquals(1, scn.GetWoundsOn(enquea));
		assertEquals(0, scn.GetWoundsOn(lemenya));

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(enquea));
		scn.ShadowUseCardAction(enquea);
		assertEquals(0, scn.GetWoundsOn(enquea));
		assertEquals(2, scn.GetTwilight());
		assertEquals(1, scn.GetBurdens());

		// Healed again: 2 twilight left and no wound to pay with.
		scn.FreepsPass();
		assertFalse(scn.ShadowActionAvailable(enquea));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AbilityIsNotOfferedWhileAnotherMinionSkirmishesAndIsOfferedInEnqueasOwnSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var rider = scn.GetFreepsCard("rider");
		scn.MoveCompanionsToTable(aragorn, rider);

		var enquea = scn.GetShadowCard("enquea");
		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		scn.MoveMinionsToTable(enquea, lemenya);
		scn.AddWoundsToChar(enquea, 1);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, lemenya}, new PhysicalCardImpl[]{rider, enquea});
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(3);
		scn.FreepsPass();

		// Úlairë Lemenya's skirmish: Enquea is assigned and wounded and the pool holds 3, but he is not skirmishing.
		assertTrue(scn.IsCharSkirmishing(lemenya));
		assertFalse(scn.IsCharSkirmishing(enquea));
		assertTrue(scn.IsCharAssigned(enquea));
		assertEquals(1, scn.GetWoundsOn(enquea));
		assertEquals(3, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(enquea));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Lemenya's 9 beats Aragorn's 8; then Enquea's own skirmish, with nothing else changed.
		scn.ShadowPass();
		scn.FreepsResolveSkirmish(rider);
		scn.FreepsPass();

		assertTrue(scn.IsCharSkirmishing(enquea));
		assertEquals(1, scn.GetWoundsOn(enquea));
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.ShadowActionAvailable(enquea));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
