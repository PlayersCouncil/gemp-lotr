package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_054_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "15_54");
					// Legolas, Fleet-footed Hunter: an [elven] Elf hunter companion; his regroup ability is used only on purpose
					put("legolas", "15_18");
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man companion who is no hunter; Knight is his whole text
					put("uruk", "15_171");      // Tracking Uruk: a hunter who is no companion, strength 14; Hunter 1 is its whole text
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, strength 13
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AragornStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Aragorn, Swift Hunter
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 4
		 * Resistance: 8
		 * Game Text: <b>Ranger</b>. <b>Hunter 1</b>. <helper>(While skirmishing a non-hunter character, this character is strength +1.)</helper><br><b>Skirmish:</b> Exert Aragorn to make a hunter companion strength +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("aragorn");

		assertEquals("Aragorn", card.getBlueprint().getTitle());
		assertEquals("Swift Hunter", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RANGER));
		assertTrue(scn.HasKeyword(card, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.HUNTER));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(8, card.getBlueprint().getResistance());
	}

	@Test
	public void SkirmishAbilityExertsAragornToMakeAHunterCompanionStrengthPlusOneForTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var knight = scn.GetFreepsCard("knight");
		var uruk = scn.GetShadowCard("uruk");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn, legolas, knight);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk, troop);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// The assignment phase, next to the skirmish phase: not offered, though Aragorn can exert.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.FreepsActionAvailable(aragorn));

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, uruk}, new PhysicalCardImpl[]{knight, troop});
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(6, scn.GetStrength(legolas));
		assertTrue(scn.FreepsActionAvailable(aragorn));
		scn.FreepsUseCardAction(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// Hunter companions: Aragorn himself, in this skirmish, and an [elven] Elf in none; not a companion who is
		// no hunter, a hunter who is no companion, or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, legolas));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(knight, uruk, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(legolas);

		// Legolas is in no skirmish, so his hunter keyword adds nothing: 6 + 1.
		assertEquals(7, scn.GetStrength(legolas));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(knight));

		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(7, scn.GetStrength(legolas));
		scn.FreepsPass();

		// The next skirmish is a phase of its own: the bonus is gone.
		scn.FreepsResolveSkirmish(knight);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(6, scn.GetStrength(legolas));
	}

	@Test
	public void WithAragornExhaustedTheSkirmishAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var uruk = scn.GetShadowCard("uruk");
		scn.MoveCompanionsToTable(aragorn, legolas);

		scn.StartGame();
		// Vitality 4: exhausted.
		scn.AddWoundsToChar(aragorn, 3);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		scn.MoveMinionsToTable(uruk);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, uruk);
		scn.FreepsResolveSkirmish(aragorn);

		// Legolas is a hunter companion who could be strengthened; Aragorn cannot pay.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertFalse(scn.FreepsActionAvailable(aragorn));
	}
}
