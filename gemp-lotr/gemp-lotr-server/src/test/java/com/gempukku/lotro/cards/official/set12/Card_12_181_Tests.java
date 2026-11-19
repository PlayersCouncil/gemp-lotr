package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_181_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("life", "12_181");
					put("life2", "12_181");
					// Úlairë Lemenya, Lieutenant of Morgul: a [wraith] Nazgûl, strength 9, vitality 2; his text reaches only a
					// character bearing a [wraith] condition, and none is laid
					put("lemenya", "1_232");
					// Ulaire Attea, Khamûl of the East: a [raider] Nazgûl, strength 12, vitality 3; the rest of his text needs
					// 3 burdens
					put("khamul", "103_61");
					// Morgul Hound: a [wraith] minion who is an Orc and no Nazgûl; strength 5, vitality 2, nothing below 2 wounds
					// on the Ring-bearer
					put("hound", "7_192");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Glóin: a skirmish ability used only on purpose
					put("legolas", "1_50");     // Legolas, Greenleaf: an archery ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: a skirmish ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: cost 2, no requirement to play; abilities used only on purpose
					// Barliman Butterbur: an ally, the Free Peoples character who is no companion; a fellowship ability used
					// only on purpose
					put("barliman", "1_70");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UnendingLifeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Unending Life
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: While you can spot 6 companions, each Nazgûl is strength +3.<br><b>Skirmish:</b> Discard this condition to heal a Nazgûl.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("life");

		assertEquals("Unending Life", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachNazgulIsStrengthPlus3OnceSixCompanionsCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var life = scn.GetShadowCard("life");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable("aragorn", "gimli", "legolas", "boromir");
		scn.MoveAlliesToTable("barliman");
		scn.MoveCardsToHand(sam);

		scn.StartGame();
		assertInHand(sam);
		scn.MoveCardsToSupportArea(life);
		scn.MoveMinionsToTable(lemenya, khamul, hound);
		scn.FreepsRefreshDecision();

		// Frodo and four companions, and an ally beside them: five companions.
		assertEquals(9, scn.GetStrength(lemenya));
		assertEquals(12, scn.GetStrength(khamul));
		assertEquals(5, scn.GetStrength(hound));

		scn.FreepsPlayCard(sam);
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertEquals(12, scn.GetStrength(lemenya));
		assertEquals(15, scn.GetStrength(khamul));
		assertEquals(5, scn.GetStrength(hound));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityDiscardsThisCopyToHealANazgulAndIsNotOfferedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var life = scn.GetShadowCard("life");
		var life2 = scn.GetShadowCard("life2");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveCardsToSupportArea(life, life2);
		scn.MoveMinionsToTable(lemenya, khamul, hound);
		scn.AddWoundsToChar(lemenya, 1);
		scn.AddWoundsToChar(khamul, 2);
		scn.AddWoundsToChar(hound, 1);
		assertInZone(Zone.SUPPORT, life, life2);
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(2, scn.GetWoundsOn(khamul));
		assertEquals(1, scn.GetWoundsOn(hound));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(life));
		assertFalse(scn.ShadowActionAvailable(life2));
		scn.ShadowPass();

		// Lemenya skirmishes Aragorn; Khamûl and the Hound are left unassigned.
		scn.FreepsAssignToMinions(aragorn, lemenya);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(life));
		scn.ShadowUseCardAction(life);
		assertInDiscard(life);
		assertInZone(Zone.SUPPORT, life2);

		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound));
		scn.ShadowChooseCard(khamul);
		assertEquals(1, scn.GetWoundsOn(khamul));
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(1, scn.GetWoundsOn(hound));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoWoundedNazgulTheSkirmishAbilityDiscardsItForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var life = scn.GetShadowCard("life");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveCardsToSupportArea(life);
		scn.MoveMinionsToTable(lemenya, hound);
		scn.AddWoundsToChar(hound, 1);
		assertInZone(Zone.SUPPORT, life);
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(1, scn.GetWoundsOn(hound));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lemenya);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(life));
		scn.ShadowUseCardAction(life);
		assertInDiscard(life);
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(1, scn.GetWoundsOn(hound));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
