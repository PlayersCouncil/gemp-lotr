package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_089_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ominous", "6_89");
					put("ominous2", "6_89");
					// Úlairë Nelya, Ringwraith in Twilight: a [wraith] Nazgûl; fierce and damage +1 only at 2 burdens or
					// 2 wounds on the Ring-bearer
					put("nelya", "2_84");
					// Ulaire Attea, Khamul of the East: a [raider] Nazgûl; fierce, and his texts read burdens only
					put("khamul", "103_61");
					// Úlairë Lemenya, Lieutenant of Morgul: a [wraith] Nazgûl of vitality 2; his text is about [wraith]
					// conditions borne
					put("lemenya", "1_232");
					// Morgul Predator: a [wraith] Orc, no Nazgûl; a skirmish ability used only on purpose
					put("predator", "7_196");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WingedandOminousStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Winged and Ominous
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.
		 * <b>Regroup:</b> Exert a Nazgûl to place a [wraith] token on this card.
		 * <b>Shadow:</b> Remove 3 [wraith] tokens from here to add (2).
		*/

		var scn = GetScenario();

		var ominous = scn.GetFreepsCard("ominous");

		assertEquals("Winged and Ominous", ominous.getBlueprint().getTitle());
		assertNull(ominous.getBlueprint().getSubtitle());
		assertFalse(ominous.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, ominous.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, ominous.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, ominous.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(ominous, Keyword.SUPPORT_AREA));
		assertEquals(1, ominous.getBlueprint().getTwilightCost());
	}

	@Test
	public void RegroupAbilityExertsANazgulOfEitherCultureToPlaceAWraithTokenOnThatCopy() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ominous = scn.GetShadowCard("ominous");
		var ominous2 = scn.GetShadowCard("ominous2");
		var nelya = scn.GetShadowCard("nelya");
		var khamul = scn.GetShadowCard("khamul");
		var predator = scn.GetShadowCard("predator");
		scn.MoveCardsToSupportArea(ominous, ominous2);
		assertEquals(0, scn.GetCultureTokensOn(ominous));
		assertEquals(0, scn.GetCultureTokensOn(ominous2));

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, khamul, predator);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		// Khamul is fierce: a fierce assignment phase, with nobody assigned again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertEquals(0, scn.GetWoundsOn(khamul));
		assertTrue(scn.ShadowActionAvailable(ominous));
		scn.ShadowUseCardAction(ominous);

		// A Nazgûl of either culture; not the [wraith] Orc who is no Nazgûl.
		assertTrue(scn.ShadowHasCardChoiceAvailable(nelya, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(predator));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(khamul);

		assertEquals(1, scn.GetWoundsOn(khamul));
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertEquals(0, scn.GetWoundsOn(predator));
		// The token goes on the copy used, and not on the other.
		assertEquals(1, scn.GetCultureTokensOn(ominous));
		assertEquals(0, scn.GetCultureTokensOn(ominous2));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWithEveryNazgulExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ominous = scn.GetShadowCard("ominous");
		var lemenya = scn.GetShadowCard("lemenya");
		var predator = scn.GetShadowCard("predator");
		scn.MoveCardsToSupportArea(ominous);
		assertEquals(0, scn.GetCultureTokensOn(ominous));

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, predator);
		// Vitality 2 with a wound: Lemenya is exhausted.  The Predator could exert.
		scn.AddWoundsToChar(lemenya, 1);
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(predator));
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(ominous));
		assertEquals(0, scn.GetCultureTokensOn(ominous));
		assertEquals(0, scn.GetWoundsOn(predator));
	}

	@Test
	public void ShadowAbilityRemoves3TokensFromThatCopyToAdd2AndIsNotOfferedOnACopyWithTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ominous = scn.GetShadowCard("ominous");
		var ominous2 = scn.GetShadowCard("ominous2");
		scn.MoveCardsToSupportArea(ominous, ominous2);

		scn.StartGame();
		scn.AddTokensToCard(ominous, 4);
		scn.AddTokensToCard(ominous2, 2);
		assertEquals(4, scn.GetCultureTokensOn(ominous));
		assertEquals(2, scn.GetCultureTokensOn(ominous2));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		int pool = scn.GetTwilight();
		// Two tokens here: not enough, though the other copy holds four.
		assertFalse(scn.ShadowActionAvailable(ominous2));
		assertTrue(scn.ShadowActionAvailable(ominous));
		scn.ShadowUseCardAction(ominous);

		// Three of the four go, and the other copy keeps its two.
		assertEquals(1, scn.GetCultureTokensOn(ominous));
		assertEquals(2, scn.GetCultureTokensOn(ominous2));
		assertEquals(pool + 2, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(ominous));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
