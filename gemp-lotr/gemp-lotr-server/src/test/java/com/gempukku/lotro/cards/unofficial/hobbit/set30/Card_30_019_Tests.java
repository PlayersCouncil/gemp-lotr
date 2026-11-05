package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_019_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. The fellowship phase
	// of turn 1 is at Bag End (site 1), whose Fellowship action plays a [dwarven] follower from hand; the maneuver
	// phase is at The Troll Hoard (site 2), whose only text is a start-of-turn offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("thorin", "30_19");
					put("balin", "30_2");       // Balin, Brother of Dwalin: a [dwarven] companion; a fellowship ability only

					// Followers. Bifur: [dwarven] Dwarf, strength +1; Aid - exert a companion; a fierce-win trigger.
					// Roäc: [dwarven] Bird, strength +1; Aid - exert Thorin; a maneuver ability not used.
					// Óin: [dwarven] Dwarf, strength +1; Aid - discard a [dwarven] card from hand (none in hand here).
					// Gwaihir: a [gandalf] Eagle, strength +1; Aid - discard a [gandalf] card from hand (none in hand).
					put("bifur", "30_5");
					put("roac", "32_8");
					put("oin", "30_16");
					put("gwaihir", "31_15");

					// Dwarven Axe: a [dwarven] possession (hand weapon, strength +2) for a [dwarven] character; its
					// damage bonus is only at a mountain.
					put("axe", "31_1");
					put("axe2", "31_1");
					// [dwarven] artifacts: The Arkenstone (support area) and Mithril Coat (armor, for Bilbo).
					put("arkenstone", "31_7");
					put("mithril", "31_5");
					put("glamdring", "30_29");  // Glamdring: a [gandalf] artifact

					put("runner", "31_30");     // Goblin Runner: a [moria] Orc, so that there is a maneuver phase
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void ThorinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Thorin, Oakenshield
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Dwarf
		 * Strength: 7
		 * Vitality: 4
		 * Resistance: 6
		 * Game Text: <b>Damage +1</b>.
		 * 	Thorin is strength +1 for each [dwarven] follower he bears.
		 * 	<b>Fellowship:</b> Discard a [dwarven] follower to place a [dwarven] artifact from your discard pile beneath your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("thorin");

		assertEquals("Thorin", card.getBlueprint().getTitle());
		assertEquals("Oakenshield", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void StrengthIsPlusOneForEachDwarvenFollowerHeBears() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thorin = scn.GetFreepsCard("thorin");
		var roac = scn.GetFreepsCard("roac");
		var bifur = scn.GetFreepsCard("bifur");
		scn.MoveCompanionsToTable(thorin);
		scn.AttachCardsTo(thorin, roac);
		scn.MoveCardsToSupportArea(bifur);

		scn.StartGame();
		scn.MoveMinionsToTable("runner");
		assertAttachedTo(roac, thorin);
		assertInZone(Zone.SUPPORT, bifur);
		// 7 + 1 printed on Roäc + 1 for one [dwarven] follower
		assertEquals(9, scn.GetStrength(thorin));

		scn.SkipToPhase(Phase.MANEUVER);
		// Roäc, borne already, has his Aid offered again; Bifur's is taken.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bifur, "Aid"));
		scn.FreepsChooseOptionalTrigger(bifur, "Aid");
		var bilbo = scn.GetRingBearer();
		assertEquals(0, scn.GetWoundsOn(bilbo));
		scn.FreepsChooseCard(bilbo);
		assertEquals(1, scn.GetWoundsOn(bilbo));
		scn.FreepsChooseCard(thorin);
		assertAttachedTo(bifur, thorin);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(roac, "Aid"));
		scn.FreepsDeclineOptionalTrigger();

		// 7 + 1 + 1 printed on the two followers + 2 for two [dwarven] followers
		assertEquals(11, scn.GetStrength(thorin));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void NoOtherFollowerOrDwarvenCardAndNoFollowerBorneByAnotherAddsStrength() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var gwaihir = scn.GetFreepsCard("gwaihir");
		var axe = scn.GetFreepsCard("axe");
		var oin = scn.GetFreepsCard("oin");
		var bifur = scn.GetFreepsCard("bifur");
		scn.MoveCompanionsToTable(thorin, balin);
		scn.AttachCardsTo(thorin, gwaihir, axe);
		scn.AttachCardsTo(balin, oin);
		scn.MoveCardsToSupportArea(bifur);

		scn.StartGame();
		assertAttachedTo(gwaihir, thorin);
		assertAttachedTo(axe, thorin);
		assertAttachedTo(oin, balin);
		assertInZone(Zone.SUPPORT, bifur);

		// 7 + 1 printed on Gwaihir + 2 printed on the Axe, and nothing for a [gandalf] follower, a [dwarven]
		// possession, a [dwarven] follower Balin bears or one in the support area.
		assertEquals(10, scn.GetStrength(thorin));
		// Balin: 7 + 1 printed on Óin.
		assertEquals(8, scn.GetStrength(balin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionDiscardsADwarvenFollowerToPlaceADwarvenArtifactBeneathTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thorin = scn.GetFreepsCard("thorin");
		var roac = scn.GetFreepsCard("roac");
		var bifur = scn.GetFreepsCard("bifur");
		var gwaihir = scn.GetFreepsCard("gwaihir");
		var axe = scn.GetFreepsCard("axe");
		var axe2 = scn.GetFreepsCard("axe2");
		var arkenstone = scn.GetFreepsCard("arkenstone");
		var mithril = scn.GetFreepsCard("mithril");
		var glamdring = scn.GetFreepsCard("glamdring");
		scn.MoveCompanionsToTable(thorin);
		scn.AttachCardsTo(thorin, roac, axe);
		scn.MoveCardsToSupportArea(bifur, gwaihir);
		scn.MoveCardsToDiscard(arkenstone, mithril, axe2, glamdring);

		scn.StartGame();
		assertAttachedTo(roac, thorin);
		assertAttachedTo(axe, thorin);
		assertInZone(Zone.SUPPORT, bifur, gwaihir);
		assertInDiscard(arkenstone, mithril, axe2, glamdring);
		assertNotSame(arkenstone, scn.GetFreepsBottomOfDeck());

		assertTrue(scn.FreepsActionAvailable(thorin));
		scn.FreepsUseCardAction(thorin);

		// A [dwarven] follower borne or in the support area; not a [gandalf] follower, nor a [dwarven] possession.
		assertTrue(scn.FreepsHasCardChoiceAvailable(roac, bifur));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gwaihir, axe));
		scn.FreepsChooseCard(roac);
		assertInDiscard(roac);
		assertInZone(Zone.SUPPORT, bifur, gwaihir);

		// A [dwarven] artifact that goes to the support area and one that is borne; not a [dwarven] possession, nor a
		// [gandalf] artifact, nor the follower just discarded.
		assertTrue(scn.FreepsHasCardChoiceAvailable(arkenstone, mithril));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(axe2, glamdring, roac));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(arkenstone);

		assertSame(arkenstone, scn.GetFreepsBottomOfDeck());
		assertInDiscard(mithril, axe2, glamdring, roac);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionIsNotOfferedWithNoDwarvenFollowerInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thorin = scn.GetFreepsCard("thorin");
		var gwaihir = scn.GetFreepsCard("gwaihir");
		var arkenstone = scn.GetFreepsCard("arkenstone");
		scn.MoveCompanionsToTable(thorin);
		scn.AttachCardsTo(thorin, gwaihir);
		scn.MoveCardsToDiscard(arkenstone);

		scn.StartGame();
		assertAttachedTo(gwaihir, thorin);
		assertInDiscard(arkenstone);

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(thorin));
	}

	@Test
	public void WithNoDwarvenArtifactInTheDiscardPileTheFollowerIsDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thorin = scn.GetFreepsCard("thorin");
		var bifur = scn.GetFreepsCard("bifur");
		var axe2 = scn.GetFreepsCard("axe2");
		var glamdring = scn.GetFreepsCard("glamdring");
		scn.MoveCompanionsToTable(thorin);
		scn.MoveCardsToSupportArea(bifur);
		scn.MoveCardsToDiscard(axe2, glamdring);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, bifur);
		assertInDiscard(axe2, glamdring);
		var bottom = scn.GetFreepsBottomOfDeck();

		assertTrue(scn.FreepsActionAvailable(thorin));
		scn.FreepsUseCardAction(thorin);

		// Bifur, the one [dwarven] follower, pays unasked; nothing is asked for an artifact and nothing moves.
		assertInDiscard(bifur);
		assertInDiscard(axe2, glamdring);
		assertSame(bottom, scn.GetFreepsBottomOfDeck());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
