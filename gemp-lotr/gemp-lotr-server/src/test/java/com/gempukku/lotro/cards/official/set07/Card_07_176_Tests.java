package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_176_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("servants", "7_176");

					put("lemenya", "1_232");    // Ulaire Lemenya: a [wraith] Nazgul; her text reaches only bearers of [wraith] conditions
					// Ulaire Attea, Khamul of the East: a Nazgul of [raider]; his text needs 3 burdens
					put("khamul", "103_61");
					// Morgul Hound: a [wraith] minion who is no Nazgul (an Orc); its text needs 2 wounds on the Ring-bearer
					put("hound", "7_192");

					// Cards for the Shadow player's hand: a minion (the Hound), a condition, an event and a Free Peoples card
					put("beasts", "7_149");     // Great Beasts: a [raider] support-area condition
					put("mumakil", "7_153");    // Mumakil of the Harad: a [raider] Maneuver event
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a Free Peoples companion

					// Companions in play beside Frodo, so that the fellowship can hold 3 threats; neither is an archer
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability only
					put("boromir", "1_97");     // Boromir, Son of Denethor: a skirmish ability only
					// Cards for the Free Peoples player's hand: a companion, an ally and a possession
					put("gimli", "1_13");       // Gimli, Son of Gloin
					put("grimir", "1_17");      // Grimir, Dwarven Elder
					put("cloak", "1_42");       // Elven Cloak
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DisposableServantsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Disposable Servants
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: Spot a Nazgûl to discard any number of cards from your hand. The Free Peoples player then discards any number of cards from his or her hand. For each card you discarded more than the Free Peoples player, add a threat.
		*/

		var scn = GetScenario();

		var servants = scn.GetFreepsCard("servants");

		assertEquals("Disposable Servants", servants.getBlueprint().getTitle());
		assertNull(servants.getBlueprint().getSubtitle());
		assertFalse(servants.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, servants.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, servants.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, servants.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(servants, Timeword.REGROUP));
		assertEquals(1, servants.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardingThreeAgainstTheFreePeoplesPlayersOneAddsTwoThreats() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var servants = scn.GetShadowCard("servants");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		var beasts = scn.GetShadowCard("beasts");
		var mumakil = scn.GetShadowCard("mumakil");
		var shadowGandalf = scn.GetShadowCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var cloak = scn.GetFreepsCard("cloak");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("boromir"));

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.MoveCardsToHand(servants, hound, beasts, mumakil, shadowGandalf, gimli, grimir, cloak);
		assertInHand(servants, hound, beasts, mumakil, shadowGandalf, gimli, grimir, cloak);
		assertEquals(5, scn.GetShadowHandCount());
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetThreats());

		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(servants));

		scn.ShadowPlayCard(servants);

		// Any number of the cards in his own hand, of every kind, from none to all four.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(4, scn.ShadowGetChoiceMax());
		assertTrue(scn.ShadowHasCardChoiceAvailable(hound, beasts, mumakil, shadowGandalf));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(gimli));
		scn.ShadowChooseCards(hound, beasts, mumakil);
		assertInDiscard(hound, beasts, mumakil);
		assertInHand(shadowGandalf);

		// Then the Free Peoples player, from his own hand, from none to all three.
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir, cloak));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(shadowGandalf));
		scn.FreepsChooseCards(gimli);
		assertInDiscard(gimli);
		assertInHand(grimir, cloak);

		assertEquals(2, scn.GetThreats()); // 3 discarded - 1 discarded
		assertInDiscard(servants);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void SpottingANazgulOfAnotherCultureOneDiscardedAgainstNoneAddsOneThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var servants = scn.GetShadowCard("servants");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		var beasts = scn.GetShadowCard("beasts");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("boromir"));

		scn.StartGame();
		scn.MoveMinionsToTable(khamul);
		scn.MoveCardsToHand(servants, hound, beasts, gimli, grimir);
		assertInHand(servants, hound, beasts, gimli, grimir);
		assertEquals(0, scn.GetThreats());

		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(servants));

		scn.ShadowPlayCard(servants);
		scn.ShadowChooseCards(hound);
		scn.FreepsDeclineChoosing();

		assertInDiscard(hound);
		assertInHand(beasts, gimli, grimir);
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AsManyDiscardedAsTheFreePeoplesPlayerAddsNoThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var servants = scn.GetShadowCard("servants");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		var beasts = scn.GetShadowCard("beasts");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("boromir"));

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.MoveCardsToHand(servants, hound, beasts, gimli, grimir);
		assertInHand(servants, hound, beasts, gimli, grimir);
		assertEquals(0, scn.GetThreats());

		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		scn.ShadowPlayCard(servants);
		scn.ShadowChooseCards(hound, beasts);
		scn.FreepsChooseCards(gimli, grimir);

		assertInDiscard(hound, beasts, gimli, grimir);
		assertEquals(0, scn.GetThreats());
		assertInDiscard(servants);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void FewerDiscardedThanTheFreePeoplesPlayerAddsNoThreatAndRemovesNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var servants = scn.GetShadowCard("servants");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("boromir"));

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.MoveCardsToHand(servants, hound, gimli, grimir);
		assertInHand(servants, hound, gimli, grimir);
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());

		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		scn.ShadowPlayCard(servants);
		scn.ShadowDeclineChoosing();
		scn.FreepsChooseCards(gimli, grimir);

		assertInHand(hound);
		assertInDiscard(gimli, grimir);
		// 0 against 2: no threat is added, and the one already there stays.
		assertEquals(1, scn.GetThreats());
		assertInDiscard(servants);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutANazgul() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var servants = scn.GetShadowCard("servants");
		var hound = scn.GetShadowCard("hound");
		var beasts = scn.GetShadowCard("beasts");

		scn.StartGame();
		scn.MoveMinionsToTable(hound);
		scn.MoveCardsToHand(servants, beasts);
		assertInHand(servants);

		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(3);
		scn.FreepsPass();

		// A [wraith] minion who is no Nazgul is not spotted.
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(servants));
		assertInHand(servants);
	}
}
