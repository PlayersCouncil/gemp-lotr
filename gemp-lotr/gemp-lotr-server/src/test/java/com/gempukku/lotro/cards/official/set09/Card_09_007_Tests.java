package com.gempukku.lotro.cards.official.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_09_007_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fury", "9_7");
					put("gimli", "1_13");       // Gimli, Son of Glóin: Dwarf companion, Damage +1, vitality 3; a skirmish ability used only on purpose
					put("farin", "1_11");       // Farin, Dwarven Emissary: Dwarf companion with no damage bonus, vitality 3
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally (home 3F), vitality 3; an ability used only on purpose
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally (home 3F), vitality 2; an ability used only on purpose
					// Ring of Guile: [dwarven] ring on a Dwarf, "Bearer is damage +1"; its maneuver ability is never used.
					put("guile", "9_8");
					// Huorn: a [gandalf] Tree companion with Damage +1, vitality 4; its response needs an Ent.
					put("huorn", "9_25");
					put("lorienelf", "1_53");   // Lórien Elf: Elf companion, vitality 2, no text once in play
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RingofFuryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Ring of Fury
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Artifact
		 * Subtype: Ring
		 * Strength: +1
		 * Game Text: Bearer must be a Dwarf.
		 * Bearer is <b>damage +2</b>.
		 * <b>Regroup:</b> Spot a Dwarf that is damage +X to heal X Dwarves. Discard this artifact.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fury");

		assertEquals("Ring of Fury", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.RING));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnADwarfAndMakesHimDamagePlusTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fury = scn.GetFreepsCard("fury");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var grimir = scn.GetFreepsCard("grimir");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		scn.MoveCompanionsToTable(gimli, farin, lorienelf);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(fury);

		scn.StartGame();

		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(farin, Keyword.DAMAGE));
		assertTrue(scn.FreepsPlayAvailable(fury));
		scn.FreepsPlayCard(fury);

		// Two Dwarf companions and a Dwarf ally are offered; the Elf and the Ring-bearer are not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, farin, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(lorienelf, scn.GetRingBearer()));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gimli);

		assertAttachedTo(fury, gimli);
		// His own Damage +1 and the Ring's +2.  The Dwarves who do not bear it are unchanged.
		assertEquals(3, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(farin, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(grimir, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RegroupActionSpotsTheBearerAtDamagePlusThreeToHealThreeDwarvesThenDiscardsTheRing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fury = scn.GetFreepsCard("fury");
		var guile = scn.GetFreepsCard("guile");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var grimir = scn.GetFreepsCard("grimir");
		var thrarin = scn.GetFreepsCard("thrarin");
		var huorn = scn.GetFreepsCard("huorn");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		scn.MoveCompanionsToTable(gimli, farin, huorn, lorienelf);
		scn.MoveAlliesToTable(grimir, thrarin);
		scn.AttachCardsTo(gimli, fury);
		scn.AttachCardsTo(grimir, guile);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.AddWoundsToChar(gimli, 2);
		scn.AddWoundsToChar(farin, 2);
		scn.AddWoundsToChar(grimir, 2);
		scn.AddWoundsToChar(thrarin, 1);
		scn.AddWoundsToChar(huorn, 2);
		scn.AddWoundsToChar(lorienelf, 1);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(3, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(grimir, Keyword.DAMAGE));
		assertTrue(scn.FreepsActionAvailable(fury));
		scn.FreepsUseCardAction(fury);

		// The Dwarves with a damage bonus are offered to be spotted: Gimli (+1 and the Ring's +2) and Grimir (Ring of
		// Guile's +1).  Farin and Thrarin have none; Huorn has one and is no Dwarf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(farin, thrarin, huorn, lorienelf));
		scn.FreepsChooseCard(gimli);

		// X is 3: three Dwarves, companions and allies, the wounded Tree and Elf not among them.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, farin, grimir, thrarin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(huorn, lorienelf));
		assertEquals(3, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(farin, grimir, thrarin);

		// Each healed once.
		assertEquals(1, scn.GetWoundsOn(farin));
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(thrarin));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetWoundsOn(huorn));
		assertEquals(1, scn.GetWoundsOn(lorienelf));
		assertInDiscard(fury);
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void SpottingADwarfAtDamagePlusOneHealsOneDwarfAndTheRingIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fury = scn.GetFreepsCard("fury");
		var guile = scn.GetFreepsCard("guile");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCompanionsToTable(gimli, farin);
		scn.MoveAlliesToTable(grimir);
		scn.AttachCardsTo(gimli, fury);
		scn.AttachCardsTo(grimir, guile);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.AddWoundsToChar(gimli, 2);
		scn.AddWoundsToChar(farin, 2);
		scn.AddWoundsToChar(grimir, 2);

		scn.FreepsUseCardAction(fury);
		scn.FreepsChooseCard(grimir);

		// X is 1.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(farin);

		assertEquals(1, scn.GetWoundsOn(farin));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetWoundsOn(grimir));
		assertInDiscard(fury);
		assertAttachedTo(guile, grimir);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupActionIsNotOfferedInTheFellowshipPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fury = scn.GetFreepsCard("fury");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		scn.MoveCompanionsToTable(gimli, farin);
		scn.AttachCardsTo(gimli, fury);

		scn.StartGame();
		scn.AddWoundsToChar(gimli, 1);
		scn.AddWoundsToChar(farin, 1);
		scn.FreepsRefreshDecision();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(fury));
	}
}
