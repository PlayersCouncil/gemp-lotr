package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_025_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. The skirmishes and
	// the regroup phase of turn 1 are at The Troll Hoard (site 2), whose only text is a start-of-turn offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("braga", "33_25");

					put("runner", "31_30");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; its play trigger never met (cheated in)
					// Malevolent Orc: [sauron] Orc; its end-of-Shadow-phase offer finds no event in the discard pile
					put("malevolent", "33_47");
					put("footman", "30_42");    // Goblin Footman: [moria] Orc, laid in the discard pile; never played
					// Alfrid, Lickspittle: unique [men] Man, strength 4; a trigger on an [esgaroth] ally assigned,
					// a maneuver play from the discard pile and a skirmish ability, none of them met or used
					put("alfrid", "32_25");

					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion, strength 7, vitality 3; a fellowship ability not used
					put("nori", "30_15");       // Nori, Of the Blue Mountains: [dwarven] companion, strength 7, vitality 3; a maneuver ability not used
					put("bard", "30_22");       // Bard, The Bowman: [esgaroth] Man ally, strength 6; a regroup ability not used
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void BragaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Braga, Captain of the Guard
		 * Unique: True
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: <b>Fierce</b>.
		 * <b>Skirmish:</b> Place an Orc from play beneath your draw deck to make a [men] Man strength +3 until the regroup phase.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("braga");

		assertEquals("Braga", card.getBlueprint().getTitle());
		assertEquals("Captain of the Guard", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlacesAnOrcFromPlayBeneathTheDrawDeckToMakeAMenManStrengthPlus3UntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var braga = scn.GetShadowCard("braga");
		var runner = scn.GetShadowCard("runner");
		var malevolent = scn.GetShadowCard("malevolent");
		var footman = scn.GetShadowCard("footman");
		var alfrid = scn.GetShadowCard("alfrid");
		var balin = scn.GetFreepsCard("balin");
		var nori = scn.GetFreepsCard("nori");
		var bard = scn.GetFreepsCard("bard");
		scn.MoveCompanionsToTable(balin, nori);

		scn.StartGame();
		scn.MoveAlliesToTable(bard);
		scn.MoveMinionsToTable(braga, runner, malevolent, alfrid);
		scn.MoveCardsToDiscard(footman);
		assertInZone(Zone.DISCARD, footman);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{balin, braga}, new PhysicalCardImpl[]{nori, runner});
		// The Malevolent Orc and Alfrid are left out of the skirmishes.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(balin);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		var top = scn.GetShadowTopOfDeck();
		int deckCount = scn.GetShadowDeckCount();
		assertEquals(9, scn.GetStrength(braga));
		assertEquals(4, scn.GetStrength(alfrid));
		assertEquals(6, scn.GetStrength(bard));
		assertTrue(scn.ShadowActionAvailable(braga));
		scn.ShadowUseCardAction(braga);
		// Orcs of two cultures in play, one assigned and one not; not a minion who is no Orc, nor an Orc in the
		// discard pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, malevolent));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(braga, alfrid, footman));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(malevolent);
		assertInZone(Zone.DECK, malevolent);
		assertSame(malevolent, scn.GetShadowBottomOfDeck());
		assertSame(top, scn.GetShadowTopOfDeck());
		assertEquals(deckCount + 1, scn.GetShadowDeckCount());

		// [men] Men, Braga skirmishing and Alfrid not; not a Man of another culture, nor an Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(braga, alfrid));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bard, runner));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(braga);
		assertEquals(12, scn.GetStrength(braga));
		assertEquals(4, scn.GetStrength(alfrid));
		assertEquals(6, scn.GetStrength(bard));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.PassSkirmishActions();
		// 12 against 7: Balin takes a wound.
		assertEquals(1, scn.GetWoundsOn(balin));
		scn.FreepsResolveSkirmish(nori);
		// The next skirmish: the bonus holds.
		assertEquals(12, scn.GetStrength(braga));
		scn.PassSkirmishActions();

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(nori, braga);
		scn.FreepsResolveSkirmish(nori);
		// The fierce skirmish, still before the regroup phase: the bonus holds.
		assertEquals(12, scn.GetStrength(braga));
		assertEquals(0, scn.GetWoundsOn(nori));
		scn.PassFierceSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(nori));

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(9, scn.GetStrength(braga));
	}

	@Test
	public void SkirmishActionIsNotOfferedWithNoOrcInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var braga = scn.GetShadowCard("braga");
		var footman = scn.GetShadowCard("footman");
		var alfrid = scn.GetShadowCard("alfrid");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.MoveMinionsToTable(braga, alfrid);
		// An Orc in the discard pile, none in play.
		scn.MoveCardsToDiscard(footman);
		assertInZone(Zone.DISCARD, footman);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, braga);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(balin);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(braga));
		assertEquals(9, scn.GetStrength(braga));
		assertInZone(Zone.DISCARD, footman);
	}
}
