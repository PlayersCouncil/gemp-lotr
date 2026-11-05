package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_004_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("band", "4_4");
					put("band2", "4_4");
					put("savage", "4_17");      // Dunlending Savage: another [dunland] Man of strength 11, with no text

					put("sam", "1_311");        // Sam, Son of Hamfast: strength 3, vitality 4; his texts are used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BandofWildMenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Band of Wild Men
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 11
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: Each time this minion wins a skirmish, you may make it <b>fierce</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("band");

		assertEquals("Band of Wild Men", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WinningASkirmishOffersToMakeItFierceAndItFightsAgainAndIsFierceNoLongerInTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var band = scn.GetShadowCard("band");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(band);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, band);

		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();
		// Nobody has won anything yet.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(band));

		// 11 against 6: the Band wins.
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(band));
		assertFalse(scn.HasKeyword(band, Keyword.FIERCE));
		assertFalse(scn.InFierceRound());

		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.HasKeyword(band, Keyword.FIERCE));

		// A fierce minion is to be defended against a second time.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(band));
		scn.FreepsAssignToMinions(gimli, band);

		// The fierce skirmish is the last thing before the regroup phase: it is fierce through it.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.HasKeyword(band, Keyword.FIERCE));
		scn.PassFierceSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(gimli));

		// It has won a second time, and is asked a second time.  Declined here, so that what is read in the
		// regroup phase is the end of the first win's fierce.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(band));
		scn.ShadowDeclineOptionalTrigger();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.HasKeyword(band, Keyword.FIERCE));
	}

	@Test
	public void DecliningLeavesItNotFierceAndNoFierceRoundFollows() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var band = scn.GetShadowCard("band");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(band);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, band);
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(band));

		scn.ShadowDeclineOptionalTrigger();
		assertFalse(scn.HasKeyword(band, Keyword.FIERCE));
		assertFalse(scn.InFierceRound());
		assertInZone(Zone.SHADOW_CHARACTERS, band);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachCopyMakesItsOwnOfferWhenItsCompanionIsOverwhelmedOrKilledByTheWoundAndOnlyThatCopyIsMadeFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var band = scn.GetShadowCard("band");
		var band2 = scn.GetShadowCard("band2");
		var sam = scn.GetFreepsCard("sam");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(sam, gimli);

		scn.StartGame();
		// Gimli has 1 vitality left: the wound for losing kills him.
		scn.AddWoundsToChar(gimli, 2);
		scn.MoveMinionsToTable(band, band2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sam, band}, new PhysicalCardImpl[]{gimli, band2});

		// 11 against 3: Sam is overwhelmed.
		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, sam);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(band));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(band2));
		assertFalse(scn.HasKeyword(band, Keyword.FIERCE));

		scn.ShadowChooseOptionalTrigger(band);
		assertTrue(scn.HasKeyword(band, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(band2, Keyword.FIERCE));

		// 11 against 6: Gimli loses and dies of the wound.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, gimli);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(band2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(band));

		scn.ShadowChooseOptionalTrigger(band2);
		assertTrue(scn.HasKeyword(band2, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(band, Keyword.FIERCE));

		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenItLosesNorWhenAnotherDunlandManWinsBesideIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var band = scn.GetShadowCard("band");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		// Aragorn is given the strength to beat the Band: 8 + 4 against 11.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 4));
		scn.MoveMinionsToTable(band, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, savage}, new PhysicalCardImpl[]{aragorn, band});

		// 11 against 6: the Savage, another [dunland] Man, wins while the Band waits for its own skirmish.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		// The Shadow player is asked nothing: where the Band has won, he is asked here.
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// 11 against 12: the Band loses, and lives (vitality 2).
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(band));
		assertInZone(Zone.SHADOW_CHARACTERS, band);
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertFalse(scn.HasKeyword(band, Keyword.FIERCE));
		assertFalse(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
