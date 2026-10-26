package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_10_071_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("toldea", "10_71");

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
	public void UlaireToldeaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Úlairë Toldëa, Thrall of The One
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 12
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: <b>Enduring</b>.<br>Shadow cards cannot exert Úlairë Toldëa during a skirmish phase.<br><b>Skirmish:</b> Heal Úlairë Toldëa to make him <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("toldea");

		assertEquals("Úlairë Toldëa", card.getBlueprint().getTitle());
		assertEquals("Thrall of The One", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ENDURING));
		assertFalse(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ShadowCardsExertToldeaInTheManeuverAndRegroupPhasesAndNotInTheSkirmishPhaseBetween() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var toldea = scn.GetShadowCard("toldea");
		var charge = scn.GetShadowCard("charge");
		var darkness = scn.GetShadowCard("darkness");
		var winged = scn.GetShadowCard("winged");

		scn.StartGame();
		scn.MoveMinionsToTable(toldea);
		scn.MoveCardsToSupportArea(winged);
		scn.MoveCardsToHand(charge, darkness);
		scn.SetTwilight(10);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Maneuver: Toldëa is the only Nazgûl, so he is the one who pays for Relentless Charge.
		assertEquals(0, scn.GetWoundsOn(toldea));
		assertTrue(scn.ShadowPlayAvailable(charge));
		scn.ShadowPlayCard(charge);
		assertEquals(1, scn.GetWoundsOn(toldea));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, toldea);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Skirmish: 10 set + 2 companions + 2 for the site, and Relentless Charge cost nothing. Toldëa, vitality 3
		// with 1 wound, could exert, but a Shadow card cannot exert him in a skirmish phase.
		assertEquals(14, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(toldea));
		assertFalse(scn.ShadowPlayAvailable(darkness));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Toldëa, 12 + 2 for his wound, beats Aragorn's 8 without killing him (16 would).
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// Regroup: he pays for Winged and Ominous.
		scn.FreepsPass();
		assertEquals(0, scn.GetCultureTokensOn(winged));
		assertTrue(scn.ShadowActionAvailable(winged));
		scn.ShadowUseCardAction(winged);
		assertEquals(2, scn.GetWoundsOn(toldea));
		assertEquals(1, scn.GetCultureTokensOn(winged));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ShadowEventExertsAnotherNazgulAndNotToldeaWhileAnotherMinionSkirmishes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var rider = scn.GetFreepsCard("rider");
		scn.MoveCompanionsToTable(aragorn, rider);

		var toldea = scn.GetShadowCard("toldea");
		var lemenya = scn.GetShadowCard("lemenya");
		var otsea = scn.GetShadowCard("otsea");
		var darkness = scn.GetShadowCard("darkness");

		scn.StartGame();
		scn.MoveMinionsToTable(toldea, lemenya, otsea);
		scn.MoveCardsToHand(darkness);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, lemenya}, new PhysicalCardImpl[]{rider, toldea});
		// Úlairë Otsëa is left out of the fighting.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(3);
		scn.FreepsPass();

		// It is a skirmish phase, though the skirmish is not Toldëa's: his own has not begun.
		assertTrue(scn.IsCharSkirmishing(lemenya));
		assertFalse(scn.IsCharSkirmishing(toldea));
		assertTrue(scn.IsCharAssigned(toldea));
		assertEquals(0, scn.GetWoundsOn(toldea));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(otsea));
		assertTrue(scn.ShadowPlayAvailable(darkness));

		scn.ShadowPlayCard(darkness);
		// The event may exert either of the other two Nazgûl, fighting or not, and may not exert Toldëa.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, otsea));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(toldea));

		scn.ShadowChooseCard(otsea);
		assertEquals(1, scn.GetWoundsOn(otsea));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(toldea));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AFreePeoplesCardAndASiteExertToldeaInHisSkirmishAndAShadowEventCannot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rider = scn.GetFreepsCard("rider");
		var mount = scn.GetFreepsCard("mount");
		scn.MoveCompanionsToTable(rider);
		scn.AttachCardsTo(rider, mount);

		var toldea = scn.GetShadowCard("toldea");
		var darkness = scn.GetShadowCard("darkness");

		scn.StartGame();
		scn.MoveMinionsToTable(toldea);
		scn.MoveCardsToHand(darkness);

		scn.SkipToAssignments();
		assertEquals(2, scn.GetCurrentSiteNumber());
		var ettenmoors = scn.GetCurrentSite();
		scn.FreepsAssignToMinions(rider, toldea);

		assertEquals(0, scn.GetWoundsOn(toldea));
		scn.FreepsResolveSkirmish(rider);
		// Rider's Mount is a Free Peoples card: at the start of the skirmish it exerts Toldëa all the same.
		assertTrue(scn.IsCharSkirmishing(toldea));
		assertEquals(1, scn.GetWoundsOn(toldea));

		scn.SetTwilight(3);
		scn.FreepsPass();
		// The event's 3 are in the pool and Toldëa, vitality 3 with 1 wound, has an exertion left in him: he is
		// the only Nazgûl, and a Shadow card cannot exert him.
		assertEquals(3, scn.GetTwilight());
		assertFalse(scn.ShadowPlayAvailable(darkness));

		// Ettenmoors is a site and no Shadow card: its ability exerts him.
		assertTrue(scn.ShadowActionAvailable(ettenmoors));
		scn.ShadowUseCardAction(ettenmoors);
		assertEquals(2, scn.GetWoundsOn(toldea));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityHealsToldeaToMakeHimDamagePlusOneUntilTheSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var rider = scn.GetFreepsCard("rider");
		scn.MoveCompanionsToTable(aragorn, rider);

		var toldea = scn.GetShadowCard("toldea");
		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		scn.MoveMinionsToTable(toldea, lemenya);
		scn.AddWoundsToChar(toldea, 1);
		// Another wounded Nazgûl, who is not "him".
		scn.AddWoundsToChar(lemenya, 1);
		assertEquals(1, scn.GetWoundsOn(toldea));
		assertEquals(1, scn.GetWoundsOn(lemenya));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, lemenya}, new PhysicalCardImpl[]{rider, toldea});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Lemenya's skirmish: Toldëa is assigned and waiting for his own, and the text names no skirmish.
		assertTrue(scn.IsCharSkirmishing(lemenya));
		assertFalse(scn.IsCharSkirmishing(toldea));
		assertEquals(0, scn.GetKeywordCount(toldea, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(lemenya, Keyword.DAMAGE));
		assertTrue(scn.ShadowActionAvailable(toldea));

		scn.ShadowUseCardAction(toldea);
		assertEquals(0, scn.GetWoundsOn(toldea));
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(1, scn.GetKeywordCount(toldea, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(lemenya, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Still in force at the next decision of the same skirmish.
		scn.FreepsPass();
		assertEquals(1, scn.GetKeywordCount(toldea, Keyword.DAMAGE));

		// Lemenya, 9 + 2 for her wound, beats Aragorn's 8; then Toldëa's own skirmish, a phase of its own: the
		// bonus is gone, and with no wound on him the ability cannot be paid for.
		scn.ShadowPass();
		scn.FreepsResolveSkirmish(rider);
		scn.FreepsPass();
		assertTrue(scn.IsCharSkirmishing(toldea));
		assertEquals(0, scn.GetKeywordCount(toldea, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(toldea));
		assertFalse(scn.ShadowActionAvailable(toldea));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsOfferedToToldeaWhoIsAssignedToNoSkirmishAtAll() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var toldea = scn.GetShadowCard("toldea");
		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		scn.MoveMinionsToTable(toldea, lemenya);
		scn.AddWoundsToChar(toldea, 1);
		assertEquals(1, scn.GetWoundsOn(toldea));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lemenya);
		// Toldëa is left out of the fighting.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.IsCharSkirmishing(lemenya));
		assertFalse(scn.IsCharAssigned(toldea));
		assertEquals(0, scn.GetKeywordCount(toldea, Keyword.DAMAGE));
		assertTrue(scn.ShadowActionAvailable(toldea));

		scn.ShadowUseCardAction(toldea);
		assertEquals(0, scn.GetWoundsOn(toldea));
		assertEquals(1, scn.GetKeywordCount(toldea, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(lemenya, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
