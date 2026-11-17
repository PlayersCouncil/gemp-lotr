package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_029_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("introspection", "12_29");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard; a strength bonus by races is his whole text
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Wizard; a bonus beside 3 Ents only
					put("glamdring", "1_75");   // Glamdring: a Free Peoples possession on Gandalf, no condition; its ability used only on purpose
					put("host1", "5_94");       // Thundering Host: Free Peoples support-area conditions, used only on purpose
					put("host2", "5_94");
					put("bowmen", "1_264");     // Orc Bowmen: [sauron] support-area condition, inert without a [sauron] Orc
					put("weary", "4_212");      // Weary: [isengard] condition on a companion; its trigger needs an [isengard] tracker
				}},
				// Site 1, Eastemnet Gullies 4_325, is no battleground; site 4, White Rocks 4_346, is one.
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers path: site 4 is White Rocks 4_346, a battleground (its Shadow number counts mounted companions).
	// The only [gandalf] Wizard is Saruman, The White 32_24, an ally of the Hobbit sets whose home is site 3 of
	// the Hobbit path: on this path he has no home site and is at no site (CR 4.2 "ally").  He is the one printed
	// card that tells a Wizard at a battleground from the fellowship at one: every other [gandalf] Wizard is a
	// companion.
	protected VirtualTableScenario GetAllyScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("introspection", "12_29");
					put("saruman", "32_24");    // Saruman, The White: [gandalf] Wizard ally, home 3H; a Gandalf is defender +1 beside him, abilities used only on purpose
					put("host", "5_94");        // Thundering Host: a Free Peoples condition in the support area, used only on purpose
					put("bowmen", "1_264");     // Orc Bowmen: [sauron] support-area condition, inert without a [sauron] Orc
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IntrospectionStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Introspection
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: <b>Spell</b>. Choose one: Spot a [gandalf] Wizard to choose an opponent who must discard one of his or her conditions from play; or spot a [gandalf] Wizard at a battleground site to discard a condition from play.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("introspection");

		assertEquals("Introspection", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AWizardAllyIsAtNoBattlegroundWithTheFellowshipAtOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAllyScenario();

		var introspection = scn.GetFreepsCard("introspection");
		var saruman = scn.GetFreepsCard("saruman");
		var host = scn.GetFreepsCard("host");
		scn.MoveAlliesToTable(saruman);
		scn.MoveCardsToSupportArea(host);
		scn.MoveCardsToHand(introspection);

		var bowmen = scn.GetShadowCard("bowmen");
		scn.MoveCardsToSupportArea(bowmen);

		scn.StartGame();
		scn.SkipToSite(4);
		assertInZone(Zone.SUPPORT, bowmen, host);
		assertEquals("White Rocks", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));

		// Saruman is spotted, but at no battleground: the second choice does nothing, so the first is carried out
		// unasked, and the opponent's one condition goes.
		scn.FreepsPlayCard(introspection);
		assertFalse(scn.FreepsDecisionAvailable("Choose"));
		assertInDiscard(bowmen);
		assertInZone(Zone.SUPPORT, host);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
	@Test
	public void CannotBePlayedWithoutAGandalfWizardThoughAGandalfCompanionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var introspection = scn.GetFreepsCard("introspection");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(avenger);
		scn.MoveCardsToHand(introspection);

		var bowmen = scn.GetShadowCard("bowmen");
		scn.MoveCardsToSupportArea(bowmen);

		scn.StartGame();

		// The Ent Avenger is a [gandalf] companion, and no Wizard.
		assertInZone(Zone.SUPPORT, bowmen);
		assertInHand(introspection);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(introspection));

		scn.MoveCompanionsToTable(gandalf);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(introspection));
	}

	@Test
	public void AtABattlegroundTheFirstChoiceHasTheOpponentDiscardOneOfHisOwnConditions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var introspection = scn.GetFreepsCard("introspection");
		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		var host1 = scn.GetFreepsCard("host1");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, glamdring);
		scn.MoveCardsToSupportArea(host1);
		scn.MoveCardsToHand(introspection);

		var bowmen = scn.GetShadowCard("bowmen");
		var weary = scn.GetShadowCard("weary");
		scn.MoveCardsToSupportArea(bowmen);
		scn.AttachCardsTo(gandalf, weary);

		scn.StartGame();
		scn.SkipToSite(4);
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertInZone(Zone.SUPPORT, bowmen, host1);
		assertAttachedTo(weary, gandalf);
		assertAttachedTo(glamdring, gandalf);

		// Gandalf is at a battleground and conditions of both sides are in play: both choices are offered.
		scn.FreepsPlayCard(introspection);
		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertTrue(scn.FreepsChoiceAvailable("Make opponent discard a condition"));
		assertTrue(scn.FreepsChoiceAvailable("Discard condition"));
		scn.FreepsChoose("Make opponent discard a condition");

		// The Shadow player chooses which of his own conditions goes, one in his support area or one on a
		// companion; the Free Peoples condition is not his to choose.
		assertTrue(scn.ShadowHasCardChoiceAvailable(bowmen, weary));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(host1));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(weary);

		assertInDiscard(weary, introspection);
		assertInZone(Zone.SUPPORT, bowmen, host1);
		assertAttachedTo(glamdring, gandalf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtABattlegroundTheSecondChoiceDiscardsAConditionOfEitherSide() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var introspection = scn.GetFreepsCard("introspection");
		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		var host1 = scn.GetFreepsCard("host1");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, glamdring);
		scn.MoveCardsToSupportArea(host1);
		scn.MoveCardsToHand(introspection);

		var bowmen = scn.GetShadowCard("bowmen");
		var weary = scn.GetShadowCard("weary");
		scn.MoveCardsToSupportArea(bowmen);
		scn.AttachCardsTo(gandalf, weary);

		scn.StartGame();
		scn.SkipToSite(4);
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertInZone(Zone.SUPPORT, bowmen, host1);
		assertAttachedTo(weary, gandalf);
		assertAttachedTo(glamdring, gandalf);

		scn.FreepsPlayCard(introspection);
		scn.FreepsChoose("Discard condition");

		// The Free Peoples player chooses: any condition, his own or the opponent's; a possession is no condition.
		assertTrue(scn.FreepsHasCardChoiceAvailable(host1, bowmen, weary));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(glamdring));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(bowmen);

		assertInDiscard(bowmen, introspection);
		assertInZone(Zone.SUPPORT, host1);
		assertAttachedTo(weary, gandalf);
		assertAttachedTo(glamdring, gandalf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AwayFromABattlegroundOnlyTheOpponentsDiscardIsCarriedOutUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var introspection = scn.GetFreepsCard("introspection");
		var gandalf = scn.GetFreepsCard("gandalf");
		var host1 = scn.GetFreepsCard("host1");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(host1);
		scn.MoveCardsToHand(introspection);

		var bowmen = scn.GetShadowCard("bowmen");
		var weary = scn.GetShadowCard("weary");
		scn.MoveCardsToSupportArea(bowmen);
		scn.AttachCardsTo(gandalf, weary);

		scn.StartGame();
		// Eastemnet Gullies, site 1: no battleground.
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertInZone(Zone.SUPPORT, bowmen, host1);
		assertAttachedTo(weary, gandalf);

		scn.FreepsPlayCard(introspection);
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertTrue(scn.ShadowHasCardChoiceAvailable(bowmen, weary));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(host1));
		scn.ShadowChooseCard(bowmen);

		assertInDiscard(bowmen, introspection);
		assertInZone(Zone.SUPPORT, host1);
		assertAttachedTo(weary, gandalf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtABattlegroundWithNoOpponentsConditionTheSecondChoiceIsCarriedOutUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var introspection = scn.GetFreepsCard("introspection");
		var gandalf = scn.GetFreepsCard("gandalf");
		var host1 = scn.GetFreepsCard("host1");
		var host2 = scn.GetFreepsCard("host2");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(host1, host2);
		scn.MoveCardsToHand(introspection);

		scn.StartGame();
		scn.SkipToSite(4);
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertInZone(Zone.SUPPORT, host1, host2);

		// The opponent has no condition to discard: only the second choice can be carried out.
		scn.FreepsPlayCard(introspection);
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(host1, host2));
		scn.FreepsChooseCard(host1);

		assertInDiscard(host1, introspection);
		assertInZone(Zone.SUPPORT, host2);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
