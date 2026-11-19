package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_152_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("relaxation", "15_152");
					put("relaxation2", "15_152");
					// Robin Smallburrow, Shirriff Cock-Robin: a [shire] follower; Aid (2), and a trigger only on a
					// transfer
					put("robin", "18_111");
					// Daddy Twofoot, Next-door Neighbor: a [shire] follower of another title; Aid (a burden), and a
					// trigger only on a transfer (he is cheated on)
					put("twofoot", "13_144");
					// Rohan Worker: a [rohan] follower; its texts answer a transfer and a won skirmish
					put("worker", "15_135");
					put("sword", "1_299");      // Hobbit Sword: a [shire] possession, a hand weapon for a Hobbit, no text
					// Merry, From O'er the Brandywine: [shire] Hobbit companion, strength 3, vitality 4
					put("merry", "1_303");
					// Orc Miscreant: [orc] Orc minion, vitality 3. "Regroup: Exert this minion to add a burden."
					put("miscreant", "11_131");
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
					// Goblin Spear: [moria] hand weapon. "If bearer kills a companion in a skirmish, add 1 burden (or 2
					// burdens if that companion was a Hobbit)."
					put("spear", "1_182");
					// Light Shining Faintly: [shire] event. "Fellowship: Add a burden to heal a Ring-bound companion twice."
					put("light", "4_309");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetEyeScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("relaxation", "15_152");
					// Robin Smallburrow, Shirriff Cock-Robin: a [shire] follower; Aid (2), and a trigger only on a
					// transfer
					put("robin", "18_111");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: he bears Glamdring
					// Glamdring: "Fellowship or Regroup: Exert Gandalf to reveal an opponent's hand."
					put("glamdring", "1_75");
					// Eye of Barad-dûr: [sauron] event.  "Response: If a Free Peoples player reveals this card from
					// your hand, discard this card to add 2 burdens."  A Shadow card that adds two burdens at once.
					put("eye", "5_96");
					put("runner", "1_178");     // Goblin Runner: a second card in the Shadow hand
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RelaxationStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Relaxation
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Response:</b> If a Shadow card is about to add any number of burdens, discard a [shire] follower and this condition from play to prevent that and discard that Shadow card.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("relaxation");

		assertEquals("Relaxation", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardsAShireFollowerAndItselfToPreventTheBurdenAMinionIsAboutToAddAndDiscardTheMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var relaxation = scn.GetFreepsCard("relaxation");
		var relaxation2 = scn.GetFreepsCard("relaxation2");
		var robin = scn.GetFreepsCard("robin");
		var twofoot = scn.GetFreepsCard("twofoot");
		var worker = scn.GetFreepsCard("worker");
		var sword = scn.GetFreepsCard("sword");
		var merry = scn.GetFreepsCard("merry");
		var miscreant = scn.GetShadowCard("miscreant");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToSupportArea(relaxation, relaxation2, robin, worker);
		// One [shire] follower in the support area and one borne; a [shire] possession borne beside it.
		scn.AttachCardsTo(merry, twofoot, sword);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		// A minion that lived through the skirmishes stands in the regroup phase.
		scn.MoveMinionsToTable(miscreant);
		scn.FreepsRefreshDecision();

		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.SUPPORT, relaxation, relaxation2, robin, worker);
		assertAttachedTo(twofoot, merry);
		assertAttachedTo(sword, merry);
		assertEquals(0, scn.GetWoundsOn(miscreant));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPass();

		scn.ShadowUseCardAction(miscreant);
		assertEquals(1, scn.GetWoundsOn(miscreant));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(relaxation));
		scn.FreepsChooseOptionalTrigger(relaxation);

		// Either [shire] follower, in the support area or borne; not a [rohan] follower, nor a [shire] possession.
		assertTrue(scn.FreepsHasCardChoiceAvailable(robin, twofoot));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(worker, sword));
		scn.FreepsChooseCard(twofoot);

		assertInDiscard(twofoot, relaxation);
		// The other copy, and the other follower, stay.
		assertInZone(Zone.SUPPORT, relaxation2, robin, worker);
		assertAttachedTo(sword, merry);
		// The burden is not added, and the minion whose ability added it is discarded.
		assertEquals(0, scn.GetBurdens());
		assertInDiscard(miscreant);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void PreventsBothBurdensAShadowPossessionIsAboutToAddAndDiscardsThePossessionOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var relaxation = scn.GetFreepsCard("relaxation");
		var robin = scn.GetFreepsCard("robin");
		var merry = scn.GetFreepsCard("merry");
		var troop = scn.GetShadowCard("troop");
		var spear = scn.GetShadowCard("spear");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToSupportArea(relaxation, robin);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.AttachCardsTo(troop, spear);
		assertAttachedTo(spear, troop);
		assertInZone(Zone.SUPPORT, relaxation, robin);
		scn.SkipToAssignments();
		// Robin's Aid was declined on the way, at the start of the maneuver phase.
		assertInZone(Zone.SUPPORT, robin);
		scn.FreepsAssignAndResolve(merry, troop);

		assertEquals(0, scn.GetBurdens());

		// 3 against 13 + 2: Merry is overwhelmed, and the Spear is about to add 2 burdens for the Hobbit it killed.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, merry);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(relaxation));
		assertEquals(0, scn.GetBurdens());
		scn.FreepsAcceptOptionalTrigger();

		// One [shire] follower: discarded unasked.
		assertInDiscard(robin, relaxation);
		assertEquals(0, scn.GetBurdens());
		// "That Shadow card" is the Spear, and not the minion bearing it.
		assertInDiscard(spear);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void PreventsBothBurdensOfAShadowEventThatAddsTwoAtOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetEyeScenario();

		var relaxation = scn.GetFreepsCard("relaxation");
		var robin = scn.GetFreepsCard("robin");
		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		var eye = scn.GetShadowCard("eye");
		// A second card in the Shadow hand, so that the Eye is one of the cards revealed and not the hand.
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, glamdring);
		scn.MoveCardsToSupportArea(relaxation, robin);
		scn.MoveCardsToHand(eye, runner);

		scn.StartGame();

		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.SUPPORT, relaxation, robin);
		assertInZone(Zone.HAND, eye, runner);

		scn.FreepsUseCardAction(glamdring);
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.ShadowPlayAvailable(eye));
		scn.ShadowPlayCard(eye);

		// "Any number of burdens": the Eye is about to add 2.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(relaxation));
		assertEquals(0, scn.GetBurdens());
		scn.FreepsAcceptOptionalTrigger();

		// One use, and neither burden is added.
		assertInDiscard(robin, relaxation, eye);
		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.HAND, runner);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ResponseIsNotOfferedWhenAFreePeoplesCardAddsABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var relaxation = scn.GetFreepsCard("relaxation");
		var robin = scn.GetFreepsCard("robin");
		var light = scn.GetFreepsCard("light");
		scn.MoveCardsToSupportArea(relaxation, robin);
		scn.MoveCardsToHand(light);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 2);
		scn.FreepsRefreshDecision();

		assertEquals(0, scn.GetBurdens());
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertInZone(Zone.SUPPORT, relaxation, robin);
		assertTrue(scn.FreepsPlayAvailable(light));

		// Its cost is a burden, and the card that adds it is a Free Peoples one.
		scn.FreepsPlayCard(light);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(relaxation));

		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInZone(Zone.SUPPORT, relaxation, robin);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ResponseIsNotOfferedWithNoShireFollowerInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var relaxation = scn.GetFreepsCard("relaxation");
		var worker = scn.GetFreepsCard("worker");
		var sword = scn.GetFreepsCard("sword");
		var merry = scn.GetFreepsCard("merry");
		var miscreant = scn.GetShadowCard("miscreant");
		scn.MoveCompanionsToTable(merry);
		// A follower of another culture, and a [shire] card that is no follower.
		scn.MoveCardsToSupportArea(relaxation, worker);
		scn.AttachCardsTo(merry, sword);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(miscreant);
		scn.FreepsRefreshDecision();

		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.SUPPORT, relaxation, worker);
		assertAttachedTo(sword, merry);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPass();

		scn.ShadowUseCardAction(miscreant);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(relaxation));
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.SUPPORT, relaxation, worker);
		assertInZone(Zone.SHADOW_CHARACTERS, miscreant);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
