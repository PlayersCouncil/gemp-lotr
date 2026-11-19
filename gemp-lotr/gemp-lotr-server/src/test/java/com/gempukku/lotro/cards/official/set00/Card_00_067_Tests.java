package com.gempukku.lotro.cards.official.set00;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_00_067_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("frodo", "0_67");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Frodo, Mr. Underhill is the Ring-bearer of this table.
	protected VirtualTableScenario GetRingBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Hobbit Sword: a possession of cost 1, a hand weapon.  "Bearer must be a Hobbit."
					put("sword1", "1_299");
					put("sword2", "1_299");
					// Sam, Son of Hamfast: the other Hobbit; his texts are used only on purpose.
					put("sam", "1_311");
					// Mithril-coat: a [shire] artifact of cost 2.  "Bearer must be Frodo."
					put("mithril", "2_105");
					// Elven Cloak: an [elven] possession of cost 1.  "To play, spot an Elf. Bearer must be a companion."
					put("cloak", "1_42");
					// Stone Trolls: a [shire] tale, a condition of cost 2.  "To play, spot Bilbo. Plays on any character."
					put("trolls", "1_314");
					// Held: a [shire] condition of cost 1 that is no tale.  "Bearer must be Frodo."
					put("held", "6_109");
					// Phial of Galadriel, Bane of Darkness: an [elven] artifact of cost 1.  "Bearer must be a Ring-bound Hobbit."
					put("phial", "103_1");
					// Legolas, Greenleaf: the Elf the Cloak spots; an archery ability only.
					put("legolas", "1_50");
					// Bilbo, Retired Adventurer: the Bilbo Stone Trolls spots, an ally; a fellowship ability used only on purpose.
					put("bilbo", "1_284");
				}},
				VirtualTableScenario.FellowshipSites,
				"0_67",
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FrodoStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 0
		 * Name: Frodo, Mr. Underhill
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 10
		 * Signet: Gandalf
		 * Game Text: <b>Ring-bearer (resistance 10).</b><br>The twilight cost of each artifact, possession, and [shire] tale played on Frodo is -1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("frodo");

		assertEquals("Frodo", card.getBlueprint().getTitle());
		assertEquals("Mr. Underhill", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(10, card.getBlueprint().getResistance());
		assertEquals(Signet.GANDALF, card.getBlueprint().getSignet());
		assertTrue(card.getBlueprint().canStartWithRing());
	}

	@Test
	public void APossessionPlayedOnFrodoCostsOneLessAndOnAnotherHobbitItsFullCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingBearerScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var sword1 = scn.GetFreepsCard("sword1");
		var sword2 = scn.GetFreepsCard("sword2");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(sword1, sword2);

		scn.StartGame();

		assertEquals("Mr. Underhill", frodo.getBlueprint().getSubtitle());
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(sword1));

		scn.FreepsPlayCard(sword1);
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam));
		scn.FreepsChooseCard(frodo);
		assertAttachedTo(sword1, frodo);
		// 1 for the Sword - 1 on Frodo
		assertEquals(0, scn.GetTwilight());

		// Frodo has his hand weapon: the second Sword can only go to Sam, at its printed cost.
		assertTrue(scn.FreepsPlayAvailable(sword2));
		scn.FreepsPlayCard(sword2);
		assertAttachedTo(sword2, sam);
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnArtifactAPossessionOfAnotherCultureAndAShireTaleCostOneLessOnFrodoAndAConditionThatIsNoTaleDoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingBearerScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var legolas = scn.GetFreepsCard("legolas");
		var bilbo = scn.GetFreepsCard("bilbo");
		var mithril = scn.GetFreepsCard("mithril");
		var cloak = scn.GetFreepsCard("cloak");
		var trolls = scn.GetFreepsCard("trolls");
		var held = scn.GetFreepsCard("held");
		var phial = scn.GetFreepsCard("phial");
		scn.MoveCompanionsToTable(sam, legolas);
		scn.MoveAlliesToTable(bilbo);
		scn.MoveCardsToHand(mithril, cloak, trolls, held, phial);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(mithril);
		assertAttachedTo(mithril, frodo);
		// 2 for the artifact - 1 on Frodo
		assertEquals(1, scn.GetTwilight());

		scn.FreepsPlayCard(phial);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam));
		scn.FreepsChooseCard(frodo);
		assertAttachedTo(phial, frodo);
		// 1 for an [elven] artifact - 1 on Frodo; a discount for [shire] artifacts alone would leave 2
		assertEquals(1, scn.GetTwilight());

		scn.FreepsPlayCard(cloak);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam, legolas));
		scn.FreepsChooseCard(frodo);
		assertAttachedTo(cloak, frodo);
		// 1 for an [elven] possession - 1 on Frodo
		assertEquals(1, scn.GetTwilight());

		scn.FreepsPlayCard(trolls);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam, bilbo));
		scn.FreepsChooseCard(frodo);
		assertAttachedTo(trolls, frodo);
		// 2 for the [shire] tale - 1 on Frodo
		assertEquals(2, scn.GetTwilight());

		scn.FreepsPlayCard(held);
		assertAttachedTo(held, frodo);
		// A [shire] condition that is no tale: its full 1
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
