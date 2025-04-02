package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.at.AbstractAtTest;
import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class Card_01_009_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("axe", "1_9");
					put("gimli", "1_13");
					put("preparations", "7_12");
					put("dcard", "2_10");

					put("orc1", "1_178");
					put("orc2", "1_181");
					put("orc3", "1_184");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void DwarvenAxeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Dwarven Axe
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Possession
		 * Subtype: Hand Weapon
		 * Strength: 2
		 * Game Text: Bearer must be a Dwarf.
		 * Each time a minion loses a skirmish to bearer, that minion's owner must discard the top card of their draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("axe");
		assertEquals("Dwarven Axe", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void AxeMustBeBorneByDwarf() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var gimli = scn.GetFreepsCard("gimli");
		scn.FreepsMoveCardToHand(axe, gimli);

		scn.StartGame();
		assertFalse(scn.FreepsPlayAvailable(axe));
		scn.FreepsPlayCard(gimli);
		assertTrue(scn.FreepsPlayAvailable(axe));
	}

	@Test
	public void AxeCausesDeckDiscardOnSkirmishVictory() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var gimli = scn.GetFreepsCard("gimli");
		scn.FreepsMoveCharToTable(gimli);
		scn.AttachCardsTo(gimli, axe);

		var orc1 = scn.GetShadowCard("orc1");
		scn.ShadowMoveCharToTable(orc1);

		scn.StartGame();

		scn.SkipToPhase(Phase.ASSIGNMENT);

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(gimli, orc1);

		scn.FreepsResolveSkirmish(gimli);
		scn.PassCurrentPhaseActions();

		var topcard = scn.GetShadowTopOfDeck();
		assertEquals(Zone.DECK, topcard.getZone());
		assertEquals(6, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetShadowDiscardCount());

		scn.FreepsResolveActionOrder("Axe");
		assertEquals(Zone.DISCARD, topcard.getZone());
		assertEquals(5, scn.GetShadowDeckCount());
		assertEquals(2, scn.GetShadowDiscardCount());//orc1, and a card discarded from the deck

	}

	@Test
	public void AxeAffectsMaxOneMinionDuringSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var gimli = scn.GetFreepsCard("gimli");
		scn.FreepsMoveCharToTable(gimli);
		scn.AttachCardsTo(gimli, axe);

		var orc1 = scn.GetShadowCard("orc1");
		var orc2 = scn.GetShadowCard("orc2");
		scn.ShadowMoveCharToTable(orc1, orc2);

		scn.StartGame();

		scn.SkipToPhase(Phase.ASSIGNMENT);

		scn.PassCurrentPhaseActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowAssignToMinions(gimli, orc1, orc2);

		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsUseCardAction(gimli);
		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsUseCardAction(gimli);
		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();

		//Should only have "resolve skirmish wounds" and one instance of "dwarven axe required trigger".
		// If there are two axe-triggers, then the card is violating the latest Decipher "clarification":
		//
		// https://wiki.lotrtcgpc.net/wiki/Comprehensive_Rules_4.1#Section_Three:_Individual_Card_Rulings
		//"This card can trigger only once for each Shadow player with a minion in that skirmish,
		// regardless of how many minions that player had."
		assertEquals(3, scn.FreepsGetAvailableActions().size());

		var topcard = scn.GetShadowTopOfDeck();
		assertEquals(Zone.DECK, topcard.getZone());
		assertEquals(5, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetShadowDiscardCount());

		//Ideally we shouldn't have both triggers, but at least we can squelch the results of the second
		scn.FreepsResolveActionOrder("Axe");
		scn.FreepsResolveActionOrder("Axe");
		assertEquals(Zone.DISCARD, topcard.getZone());
		assertEquals(4, scn.GetShadowDeckCount());
		assertEquals(3, scn.GetShadowDiscardCount());//2 dead runners, and a card discarded from the deck
	}


	/**
	 * https://github.com/PlayersCouncil/gemp-lotr/issues/571
	 * Basic issue is that based on the last CRD:
	 * 	 > A losing character is any character on the losing side when a skirmish revolves.
	 * 	 > If a character is removed from his or her skirmish and there are still one or more characters
	 * 	 > on each side of that skirmish, the removed character is neither a losing nor a winning character.
	 * 	 > A character removed from a skirmish is not wounded (or overwhelmed) when that skirmish resolves.
	 * This supercedes the Comprehensive Rules 4.0, which states:
	 *   > A losing character is any character on the losing side in a skirmish when it resolves.
	 *   > Also, any character removed during his or her skirmish is a losing character, even if
	 *   > that character’s side eventually wins.
	 *
	 * From a basic functionality standpoint, killed characters should not stick around as part of the
	 * tracked losing characters lists, or at the very least the skirmish resolution should check to see
	 * if they are still in the skirmish and disqualify them for trigger emission if so.
	 */

	@Test
	public void AxeDoesNotTriggerOnMinionKilledBeforeSkirmishResolves() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var gimli = scn.GetFreepsCard("gimli");
		var preparations = scn.GetFreepsCard("preparations");
		scn.FreepsMoveCharToTable(gimli);
		scn.AttachCardsTo(gimli, axe);
		scn.FreepsMoveCardToSupportArea(preparations);
		scn.FreepsStackCardsOn(preparations, "dcard");

		var orc1 = scn.GetShadowCard("orc1");
		var orc2 = scn.GetShadowCard("orc2");
		var orc3 = scn.GetShadowCard("orc3");
		scn.ShadowMoveCharToTable(orc1, orc2, orc3);

		scn.StartGame();

		scn.SkipToPhase(Phase.ASSIGNMENT);

		scn.PassCurrentPhaseActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowAssignToMinions(gimli, orc1, orc2, orc3);

		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsUseCardAction(preparations);
		scn.FreepsChooseCard(orc3);
		assertEquals(Zone.DISCARD, orc3.getZone());

		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();

		//There should not be any axe trigger here; we should go straight to regroup

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
	}

	@Test
	public void Legacy_dwarvenAxeDoesNotFreeze() throws DecisionResultInvalidException, CardNotFoundException {
		var legacy = new LegacyAtTest();

		legacy.dwarvenAxeDoesNotFreeze();
	}

	public static class LegacyAtTest extends AbstractAtTest {

		public void dwarvenAxeDoesNotFreeze() throws DecisionResultInvalidException, CardNotFoundException {
			Map<String, Collection<String>> extraCards = new HashMap<>();
			initializeSimplestGame(extraCards);

			var gimli = new PhysicalCardImpl(100, "1_13", P1, _cardLibrary.getLotroCardBlueprint("1_13"));
			var dwarvenAxe = new PhysicalCardImpl(101, "1_9", P1, _cardLibrary.getLotroCardBlueprint("1_9"));
			var goblinRunner = new PhysicalCardImpl(102, "1_178", P2, _cardLibrary.getLotroCardBlueprint("1_178"));
			var cardInDeck = new PhysicalCardImpl(103, "1_178", P2, _cardLibrary.getLotroCardBlueprint("1_178"));

			skipMulligans();

			_game.getGameState().addCardToZone(_game, gimli, Zone.FREE_CHARACTERS);
			_game.getGameState().attachCard(_game, dwarvenAxe, gimli);
			_game.getGameState().addCardToZone(_game, goblinRunner, Zone.HAND);
			_game.getGameState().putCardOnTopOfDeck(cardInDeck);

			// End fellowship phase
			assertEquals(Phase.FELLOWSHIP, _game.getGameState().getCurrentPhase());
			playerDecided(P1, "");

			// Play Goblin Runner
			assertEquals(Phase.SHADOW, _game.getGameState().getCurrentPhase());
			int twilight = _game.getGameState().getTwilightPool();
			playerDecided(P2, "0");
			// Twilight is -1-2+2 (cost, roaming, effect)
			assertEquals(twilight - 1 - 2 + 2, _game.getGameState().getTwilightPool());

			// End shadow phase
			playerDecided(P2, "");

			// End maneuver phase
			playerDecided(P1, "");
			playerDecided(P2, "");

			// End archery phase
			playerDecided(P1, "");
			playerDecided(P2, "");

			// End assignment phase
			playerDecided(P1, "");
			playerDecided(P2, "");

			// Assign
			playerDecided(P1, gimli.getCardId() + " " + goblinRunner.getCardId());

			// Start skirmish
			playerDecided(P1, String.valueOf(gimli.getCardId()));

			// End skirmish phase
			playerDecided(P1, "");
			playerDecided(P2, "");

			final AwaitingDecision awaitingDecision = _userFeedback.getAwaitingDecision(P1);
			assertEquals(AwaitingDecisionType.ACTION_CHOICE, awaitingDecision.getDecisionType());
			validateContents(new String[]{"1_9", "rules"}, (String[]) awaitingDecision.getDecisionParameters().get("blueprintId"));

			playerDecided(P1, "0");

			assertEquals(Phase.REGROUP, _game.getGameState().getCurrentPhase());
			assertEquals(Zone.DISCARD, goblinRunner.getZone());

			assertEquals(Zone.DISCARD, cardInDeck.getZone());
		}
	}
}
