package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_099_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("plans", "1_99");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man ranger, vitality 4; a maneuver ability used only on purpose
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an [elven] Elf ranger, vitality 3; strength against a Nazgul only
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man who is no ranger, Knight only
					put("legolas", "1_50");     // Legolas, Greenleaf: an Elf who is no ranger; an archery ability only
					put("wrath", "4_261");      // Wrath of Harad: [raider] support condition; while a [raider] Man is spotted, each Free Peoples event that spots or exerts a ranger gains the cost "wound a companion"
					put("axeman", "4_224");     // Easterling Axeman: [raider] Man, strength 6, vitality 2, site 4; strength +3 and fierce only at 2 burdens

					// The Shadow player's hand and draw deck are made of these: Goblin Patrol Troop, a minion with no text.
					put("s1", "1_177");
					put("s2", "1_177");
					put("s3", "1_177");
					put("s4", "1_177");
					put("s5", "1_177");
					put("s6", "1_177");
					put("s7", "1_177");
					put("s8", "1_177");
					put("s9", "1_177");
					put("s10", "1_177");
					put("s11", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ChangeofPlansStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Change of Plans
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: <b>Regroup:</b> Exert a ranger to make an opponent shuffle his hand into his draw deck and draw 8 cards.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("plans");

		assertEquals("Change of Plans", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsARangerToMakeTheShadowPlayerShuffleHisHandIntoHisDeckAndDrawEight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var plans = scn.GetFreepsCard("plans");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var knight = scn.GetFreepsCard("knight");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(aragorn, arwen, knight, legolas);
		scn.MoveCardsToHand(plans);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		// A Shadow hand of 2 and a Shadow draw deck of 9.
		scn.SetShadowHand(scn.GetShadowCard("s1"), scn.GetShadowCard("s2"));
		scn.SetShadowDrawDeck(scn.GetShadowCard("s3"), scn.GetShadowCard("s4"), scn.GetShadowCard("s5"),
				scn.GetShadowCard("s6"), scn.GetShadowCard("s7"), scn.GetShadowCard("s8"),
				scn.GetShadowCard("s9"), scn.GetShadowCard("s10"), scn.GetShadowCard("s11"));

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(9, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int freepsHand = scn.GetFreepsHandCount();
		int freepsDeck = scn.GetFreepsDeckCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		assertInHand(plans);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(legolas));

		assertTrue(scn.FreepsPlayAvailable(plans));
		scn.FreepsPlayCard(plans);

		// A [gondor] Man ranger and an [elven] Elf ranger; not a [gondor] Man or an Elf who is no ranger.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, arwen));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(knight, legolas, scn.GetRingBearer()));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(arwen);

		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(legolas));

		// The Shadow player's 2 cards went into his deck, which was shuffled once, and he drew 8 of the 11:
		// nothing went to his discard pile.
		assertEquals(8, scn.GetShadowHandCount());
		assertEquals(3, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertEquals(shadowShuffles + 1, scn.GetShadowDeckShuffleCount());

		// The Free Peoples player's own hand and deck are left alone, but for the event played.
		assertInDiscard(plans);
		assertEquals(freepsHand - 1, scn.GetFreepsHandCount());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void IsNotPlayableWhileTheOnlyRangerIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var plans = scn.GetFreepsCard("plans");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(aragorn, knight, legolas);
		scn.MoveCardsToHand(plans);

		scn.StartGame();
		// Vitality 4 with 3 wounds: exhausted. The Knight and Legolas could exert, but are no rangers.
		scn.AddWoundsToChar(aragorn, 3);
		scn.SkipToPhase(Phase.REGROUP);

		assertInHand(plans);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(plans));

		// One wound off, and the event can be played.
		scn.RemoveWoundsFromChar(aragorn, 1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(plans));
	}

	@Test
	public void UnderWrathOfHaradItAlsoCostsAWoundOnACompanionOfThePlayersChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var plans = scn.GetFreepsCard("plans");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(aragorn, knight);
		scn.MoveCardsToHand(plans);

		var wrath = scn.GetShadowCard("wrath");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToSupportArea(wrath);
		assertInZone(Zone.SUPPORT, wrath);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(axeman);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetShadowHandCount());
		assertInZone(Zone.SUPPORT, wrath);
		assertInZone(Zone.SHADOW_CHARACTERS, axeman);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(plans));

		scn.FreepsPlayCard(plans);
		// The event exerts a ranger (Aragorn, the only one, unasked), so Wrath of Harad, with a [raider] Man spotted,
		// adds "wound a companion" to its cost: a cost of the Free Peoples player's event, which he pays and so
		// chooses (CR 4.2 "cost").
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, knight));
		scn.FreepsChooseCard(knight);

		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(8, scn.GetShadowHandCount());
		assertInDiscard(plans);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
