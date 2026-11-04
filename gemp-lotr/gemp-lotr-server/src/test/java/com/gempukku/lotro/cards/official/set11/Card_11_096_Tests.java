package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_096_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("targeting", "11_96");
					// Corps of Harad: a [men] Man minion, vitality 3; fierce only at a battleground, and this path has none
					put("corps", "11_73");
					// Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit; his abilities are used only on purpose
					put("ted", "18_75");
					// Goblin Marksman: a [moria] archer and nothing else, so the minion archery total starts at 1
					put("marksman", "1_176");
					put("soldier", "4_254");    // Southron Soldier: a [raider] Man; Southron and ambush (2) only
					// Goaded to War: a [men] condition that is no minion; its move trigger adds (1) and reaches no reading
					put("goaded", "12_67");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: vitality 4; his ability is never used
					// Legolas, Prince of Mirkwood: an archer, so the fellowship archery total is 1; his bonus against
					// Nazgul reaches no test
					put("legolas", "1_51");
					put("knight", "5_35");      // Gondorian Knight: strength 6, vitality 3, no other text
					// Thrarin, Dwarven Smith: an ally whose own maneuver ability (exert him) has him take part in
					// archery fire until the regroup phase
					put("thrarin", "1_27");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PrecisionTargetingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Precision Targeting
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Archery
		 * Game Text: Spot a [men] minion to make the minion archery total +1 and to make the Free Peoples player choose 3 companions. Other companions cannot take archery wounds.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("targeting");

		assertEquals("Precision Targeting", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.ARCHERY));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingAMenManAddsOneToTheMinionArcheryTotalAndOnlyTheThreeChosenCompanionsTakeItsWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var targeting = scn.GetShadowCard("targeting");
		var corps = scn.GetShadowCard("corps");
		var marksman = scn.GetShadowCard("marksman");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var knight = scn.GetFreepsCard("knight");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(aragorn, legolas, knight);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToHand(targeting);
		assertInHand(targeting);

		scn.StartGame();
		scn.MoveMinionsToTable(corps, marksman);
		scn.SkipToPhase(Phase.MANEUVER);
		// Thrarin takes part in archery fire: an ally, a character who is no companion.
		assertEquals(0, scn.GetWoundsOn(thrarin));
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();

		assertInHand(targeting);
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.ShadowPlayAvailable(targeting));
		scn.ShadowPlayCard(targeting);

		// The Free Peoples player chooses exactly 3 of his 4 companions; the ally and the minions are no choice.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, legolas, knight));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(thrarin, corps, marksman));
		assertEquals(3, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(frodo, aragorn, legolas);

		// 1 for the Marksman + 1; the fellowship's total is untouched.
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertInDiscard(targeting);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());

		// Archery fire, the total still 2: the Knight, not chosen, cannot take a wound; the chosen three can, and
		// so can Thrarin, who is no companion.
		scn.PassArcheryActions();
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, legolas, thrarin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(knight));
		scn.FreepsChooseCard(aragorn);
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(knight));
		scn.FreepsChooseCard(aragorn);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(knight));

		// The fellowship's archery wound still goes on a minion: minions are no companions.
		assertEquals(0, scn.GetWoundsOn(corps));
		assertTrue(scn.ShadowHasCardChoiceAvailable(corps, marksman));
		scn.ShadowChooseCard(corps);
		assertEquals(1, scn.GetWoundsOn(corps));

		// Gone with the archery phase.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(1, scn.GetShadowArcheryTotal());
	}

	@Test
	public void WithoutItTheCompanionLeftOutCanTakeAnArcheryWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var targeting = scn.GetShadowCard("targeting");
		var corps = scn.GetShadowCard("corps");
		var marksman = scn.GetShadowCard("marksman");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(aragorn, legolas, knight);
		scn.MoveCardsToHand(targeting);
		assertInHand(targeting);

		scn.StartGame();
		scn.MoveMinionsToTable(corps, marksman);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassArcheryActions();

		// The Marksman's one wound: every companion is offered it, the Knight among them.
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertInHand(targeting);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, legolas, knight));
		assertEquals(0, scn.GetWoundsOn(knight));
		scn.FreepsChooseCard(knight);
		assertEquals(1, scn.GetWoundsOn(knight));
		// The fellowship's own archery wound is the Shadow player's to place next.
		scn.ShadowChooseCard(corps);
		assertEquals(1, scn.GetWoundsOn(corps));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void SpottingAMenMinionWhoIsNoManAddsOneToTheMinionArcheryTotal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var targeting = scn.GetShadowCard("targeting");
		var ted = scn.GetShadowCard("ted");
		var marksman = scn.GetShadowCard("marksman");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(aragorn, knight, legolas);
		scn.MoveCardsToHand(targeting);
		assertInHand(targeting);

		scn.StartGame();
		scn.MoveMinionsToTable(ted, marksman);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();

		assertInHand(targeting);
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.ShadowPlayAvailable(targeting));
		scn.ShadowPlayCard(targeting);
		scn.FreepsChooseCards(frodo, aragorn, knight);

		assertEquals(2, scn.GetShadowArcheryTotal());
		assertInDiscard(targeting);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutAMenMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var targeting = scn.GetShadowCard("targeting");
		var marksman = scn.GetShadowCard("marksman");
		var soldier = scn.GetShadowCard("soldier");
		var goaded = scn.GetShadowCard("goaded");
		scn.MoveCardsToHand(targeting);
		assertInHand(targeting);

		scn.StartGame();
		// A [men] card that is no minion, a [moria] minion and a Man minion of [raider].
		scn.MoveCardsToSupportArea(goaded);
		scn.MoveMinionsToTable(marksman, soldier);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();

		assertInHand(targeting);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(targeting));
		assertEquals(1, scn.GetShadowArcheryTotal());
	}
}
