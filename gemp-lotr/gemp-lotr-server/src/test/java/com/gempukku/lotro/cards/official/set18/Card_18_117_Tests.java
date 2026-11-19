package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_117_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wound", "18_117");

					put("uruk1", "18_131");     // White Hand Uruk: [uruk-hai] Uruk-hai, strength 10, damage +1; a requirement to play, cheated in
					put("uruk2", "18_131");
					put("saruman", "17_116");   // Saruman, Master of the White Hand: an [uruk-hai] minion who is no Uruk-hai, strength 8; a skirmish ability used only on purpose
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] Uruk-hai, strength 9, damage +1 and nothing else
					put("spear", "17_117");     // Spear of the White Hand: an [uruk-hai] possession (hand weapon, strength +1), no text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GhastlyWoundStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Ghastly Wound
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make an [uruk-hai] minion strength +2 and <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wound");

		assertEquals("Ghastly Wound", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesTheUrukHaiMinionInTheSkirmishStrengthPlus2AndDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wound = scn.GetShadowCard("wound");
		var uruk1 = scn.GetShadowCard("uruk1");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(wound);
		assertInHand(wound);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk1);
		scn.SetTwilight(10);
		assertEquals(10, scn.GetTwilight());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, uruk1);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(10, scn.GetStrength(uruk1));
		assertEquals(1, scn.GetKeywordCount(uruk1, Keyword.DAMAGE));

		assertTrue(scn.ShadowPlayAvailable(wound));
		scn.ShadowPlayCard(wound);

		assertInDiscard(wound);
		// 10 + 2, and his printed damage +1 with one more.
		assertEquals(12, scn.GetStrength(uruk1));
		assertEquals(2, scn.GetKeywordCount(uruk1, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotPlayableInTheAssignmentPhaseAndAnyUrukHaiMinionMayBeChosenItsBonusesLastingTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wound = scn.GetShadowCard("wound");
		var uruk1 = scn.GetShadowCard("uruk1");
		var uruk2 = scn.GetShadowCard("uruk2");
		var saruman = scn.GetShadowCard("saruman");
		var guard = scn.GetShadowCard("guard");
		var spear = scn.GetShadowCard("spear");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(wound);
		assertInHand(wound);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk1, uruk2, saruman, guard);
		scn.AttachCardsTo(uruk1, spear);
		assertAttachedTo(spear, uruk1);
		scn.SetTwilight(10);
		assertEquals(10, scn.GetTwilight());

		// The assignment phase, next door to the skirmish: not playable.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(wound));
		scn.ShadowPass();

		// The Guard joins the first White Hand Uruk on Aragorn; the second is left unassigned.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, uruk1}, new PhysicalCardImpl[]{gimli, saruman});
		scn.ShadowAssignToMinions(aragorn, guard);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(wound));
		scn.ShadowPlayCard(wound);

		// An [uruk-hai] minion in this skirmish, in no skirmish, or in one still to come (the text names no
		// skirmish, F-0527); not the [sauron] Uruk-hai beside him, nor the [uruk-hai] possession.
		assertTrue(scn.ShadowHasCardChoiceAvailable(uruk1, uruk2, saruman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard, spear, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());

		assertEquals(8, scn.GetStrength(saruman));
		assertEquals(0, scn.GetKeywordCount(saruman, Keyword.DAMAGE));
		assertEquals(11, scn.GetStrength(uruk1));
		scn.ShadowChooseCard(saruman);

		assertInDiscard(wound);
		assertEquals(10, scn.GetStrength(saruman));
		assertEquals(1, scn.GetKeywordCount(saruman, Keyword.DAMAGE));
		// 10 + 1 for the Spear, untouched.
		assertEquals(11, scn.GetStrength(uruk1));
		assertEquals(9, scn.GetStrength(guard));

		// Aragorn's skirmish was the phase the bonuses lasted for: in Saruman's own they are gone.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, saruman));
		assertEquals(8, scn.GetStrength(saruman));
		assertEquals(0, scn.GetKeywordCount(saruman, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoUrukHaiMinionInPlayIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wound = scn.GetShadowCard("wound");
		var guard = scn.GetShadowCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(wound);
		assertInHand(wound);

		scn.StartGame();
		scn.MoveMinionsToTable(guard);
		scn.SetTwilight(10);
		assertEquals(10, scn.GetTwilight());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, guard);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(9, scn.GetStrength(guard));
		assertEquals(1, scn.GetKeywordCount(guard, Keyword.DAMAGE));

		assertTrue(scn.ShadowPlayAvailable(wound));
		scn.ShadowPlayCard(wound);

		assertInDiscard(wound);
		assertEquals(9, scn.GetStrength(guard));
		assertEquals(1, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
