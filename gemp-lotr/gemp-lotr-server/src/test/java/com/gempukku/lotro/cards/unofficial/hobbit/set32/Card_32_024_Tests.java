package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_024_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. The characters with a
	// start-of-turn text are cheated in after StartGame() where a test plays on past the fellowship phase.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("saruman", "32_24");
					// Gandalf, Friend of Thorin: the Hobbit sets' Gandalf, cost 1, strength 7; a Fellowship ability
					// only, and a discard bar that wants two [dwarven] followers
					put("gandalf", "30_26");
					// Gandalf, Leader of Dwarves: a second Hobbit-set Gandalf; a reconcile draw and Skirmish abilities
					// used only on purpose
					put("gandalf2", "30_25");
					// Gandalf, Friend of the Shirefolk: a Gandalf of the Decipher sets; a strength bonus only
					put("gandalf1", "1_72");
					// Elrond, Elven Lord: an [elven] Wise ally, strength 8; a start-of-turn heal offer and a Regroup
					// ability used only on purpose
					put("elrond", "30_21");
					// Radagast, The Brown: a [gandalf] Wise ally who is not Gandalf; a Fellowship ability used only on purpose
					put("radagast", "31_17");
					// Lindir, Elven Minstrel: an [elven] ally who is not Wise; a move trigger, nobody wounded
					put("lindir", "33_14");
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is not Gandalf; a strength bonus beside 3 Ents
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion who is not Wise, strength 7; a Fellowship ability

					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc, strength 6, vitality 2; an Assignment ability used only on purpose
					put("runner", "31_30");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; a play trigger only
					// Dark Sorcery: "The first sentence of Gandalf's game text does not apply"; its Shadow ability is
					// used only on purpose
					put("sorcery", "33_61");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void SarumanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Saruman, The White
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 4
		 * Type: Ally
		 * Subtype: Wizard
		 * Home: 3H
		 * Strength: 8
		 * Vitality: 4
		 * Game Text: Wise.
		 * 		Gandalf is defender +1.
		 * 		Fellowship: Discard a Wise ally to play Gandalf from your discard pile.
		 * 		Skirmish: Exert Saruman to make a Wise character strength +2 (limit +2).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("saruman");

		assertEquals("Saruman", card.getBlueprint().getTitle());
		assertEquals("The White", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.WIZARD, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.WISE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.HOBBIT, 3)));
	}

	@Test
	public void WhileSarumanIsInPlayGandalfIsDefenderPlus1AndNoOtherCharacterIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var saruman = scn.GetFreepsCard("saruman");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var radagast = scn.GetFreepsCard("radagast");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.MoveAlliesToTable(radagast, elrond);

		scn.StartGame();

		assertEquals(0, scn.GetKeywordCount(gandalf, Keyword.DEFENDER));
		scn.MoveAlliesToTable(saruman);

		assertEquals(1, scn.GetKeywordCount(gandalf, Keyword.DEFENDER));
		// Another [gandalf] companion, a [gandalf] Wizard ally, a Wise ally, the Ring-bearer, and Saruman himself.
		assertEquals(0, scn.GetKeywordCount(avenger, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(radagast, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(elrond, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(bilbo, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(saruman, Keyword.DEFENDER));
	}

	@Test
	public void DarkSorceryDoesNotTakeAwayTheDefenderBonusSarumanGivesGandalf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saruman = scn.GetFreepsCard("saruman");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveAlliesToTable(saruman);

		var sorcery = scn.GetShadowCard("sorcery");

		scn.StartGame();

		assertEquals(1, scn.GetKeywordCount(gandalf, Keyword.DEFENDER));
		// Dark Sorcery switches off a sentence of Gandalf's own text; "Gandalf is defender +1" is Saruman's.
		scn.MoveCardsToSupportArea(sorcery);
		assertInZone(Zone.SUPPORT, sorcery);
		assertEquals(1, scn.GetKeywordCount(gandalf, Keyword.DEFENDER));
	}

	@Test
	public void FellowshipActionDiscardsAWiseAllyToPlayGandalfFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var saruman = scn.GetFreepsCard("saruman");
		var elrond = scn.GetFreepsCard("elrond");
		var radagast = scn.GetFreepsCard("radagast");
		var lindir = scn.GetFreepsCard("lindir");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gandalf1 = scn.GetFreepsCard("gandalf1");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveAlliesToTable(saruman, elrond, radagast, lindir);
		scn.MoveCardsToDiscard(gandalf, gandalf1, avenger);

		// The other player's Gandalf, a title none of the Free Peoples player's piles holds, in his own discard pile.
		var shadowgandalf2 = scn.GetShadowCard("gandalf2");
		scn.MoveCardsToDiscard(shadowgandalf2);

		scn.StartGame();
		assertInDiscard(gandalf, gandalf1, avenger, shadowgandalf2);

		assertTrue(scn.FreepsActionAvailable(saruman));
		scn.FreepsUseCardAction(saruman);

		assertTrue(scn.FreepsHasCardChoiceAvailable(saruman, elrond, radagast));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(lindir, bilbo));
		scn.FreepsChooseCard(elrond);
		assertInDiscard(elrond);

		assertTrue(scn.FreepsDecisionAvailable("Choose card from discard"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, gandalf1));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(avenger, shadowgandalf2));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(gandalf);

		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertInDiscard(gandalf1, avenger);
		assertInZone(Zone.SUPPORT, saruman, radagast, lindir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionIsNotOfferedWithoutAGandalfInTheDiscardPileThatCanBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saruman = scn.GetFreepsCard("saruman");
		var elrond = scn.GetFreepsCard("elrond");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gandalf2 = scn.GetFreepsCard("gandalf2");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveAlliesToTable(saruman, elrond);
		scn.MoveCardsToDiscard(avenger);

		scn.StartGame();

		// Only a [gandalf] companion who is no Gandalf in the discard pile, and two Wise allies to pay with.
		assertInDiscard(avenger);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(saruman));

		scn.MoveCardsToDiscard(gandalf);
		scn.FreepsRefreshDecision();
		assertInDiscard(gandalf);
		assertTrue(scn.FreepsActionAvailable(saruman));

		// Another Gandalf in play: the one in the discard pile cannot be played, Gandalf being unique.
		scn.MoveCompanionsToTable(gandalf2);
		scn.FreepsRefreshDecision();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(saruman));
		assertInZone(Zone.SUPPORT, saruman, elrond);
	}

	@Test
	public void SkirmishActionExertsSarumanToMakeAWiseCharacterStrengthPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var saruman = scn.GetFreepsCard("saruman");
		var gandalf = scn.GetFreepsCard("gandalf");
		var elrond = scn.GetFreepsCard("elrond");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(gandalf, balin);

		var aggressor = scn.GetShadowCard("aggressor");

		scn.StartGame();
		scn.MoveAlliesToTable(saruman, elrond);
		scn.MoveMinionsToTable(aggressor);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, aggressor);
		scn.FreepsResolveSkirmish(gandalf);

		assertEquals(0, scn.GetWoundsOn(saruman));
		assertEquals(7, scn.GetStrength(gandalf));
		assertTrue(scn.FreepsActionAvailable(saruman));
		scn.FreepsUseCardAction(saruman);
		assertEquals(1, scn.GetWoundsOn(saruman));

		// Gandalf in the skirmish, Elrond outside it, and Saruman himself; not Balin or Bilbo, who are not Wise, nor
		// the minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, elrond, saruman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(balin, bilbo, aggressor));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gandalf);

		assertEquals(9, scn.GetStrength(gandalf));
		assertEquals(8, scn.GetStrength(elrond));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheLimitOfPlus2HoldsForTheSkirmishAndRenewsInTheNextAndAnExhaustedSarumanCannotUseIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saruman = scn.GetFreepsCard("saruman");
		var gandalf = scn.GetFreepsCard("gandalf");
		var elrond = scn.GetFreepsCard("elrond");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(gandalf, balin);

		var aggressor = scn.GetShadowCard("aggressor");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveAlliesToTable(saruman, elrond);
		scn.MoveMinionsToTable(aggressor, runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gandalf, aggressor}, new PhysicalCardImpl[]{balin, runner});

		// Gandalf's skirmish first.
		scn.FreepsResolveSkirmish(gandalf);
		scn.FreepsUseCardAction(saruman);
		scn.FreepsChooseCard(gandalf);
		assertEquals(9, scn.GetStrength(gandalf));
		scn.ShadowPass();

		// The +2 is spent for this skirmish: offered again, Saruman exerts, and Elrond gains nothing.
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertTrue(scn.FreepsActionAvailable(saruman));
		scn.FreepsUseCardAction(saruman);
		assertEquals(2, scn.GetWoundsOn(saruman));
		scn.FreepsChooseCard(elrond);
		assertEquals(8, scn.GetStrength(elrond));
		assertEquals(9, scn.GetStrength(gandalf));

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(balin);

		// A skirmish is a phase of its own: Gandalf's +2 is gone, and the limit is fresh.
		assertEquals(7, scn.GetStrength(gandalf));
		assertTrue(scn.FreepsActionAvailable(saruman));
		scn.FreepsUseCardAction(saruman);
		scn.FreepsChooseCard(elrond);
		assertEquals(10, scn.GetStrength(elrond));
		scn.ShadowPass();

		// Vitality 4 with three wounds: exhausted.
		assertEquals(3, scn.GetWoundsOn(saruman));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(saruman));
	}
}
