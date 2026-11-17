package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_039_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("glamdring", "7_39");
					put("gandalf", "1_364");      // Gandalf, The Grey Wizard: cost 4, vitality 4; a fellowship ability never used here
					put("avenger", "6_27");       // Ent Avenger: a [gandalf] companion who is not Gandalf; stronger only beside 3 Ents
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: strength 8; a maneuver ability never used here

					// Cards for the hands: a companion, a possession and an event, of three cultures
					put("legolas", "1_50");       // Legolas, Greenleaf: [elven] companion
					put("armor", "1_8");          // Dwarven Armor: [dwarven] possession
					put("deftness", "1_293");     // Halfling Deftness: [shire] Skirmish event

					put("party", "1_158");        // Uruk-hai Raiding Party: [isengard] Uruk-hai, vitality 3, damage +1 and nothing else
					put("troop", "1_177");        // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("runner", "1_178");       // Goblin Runner: vitality 1, so he cannot exert; adds (2) when played, which he never is here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GlamdringStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Glamdring, Elven Blade
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: 2
		 * Game Text: Bearer must be Gandalf.<br>He is <b>damage +1</b>.<br><b>Skirmish:</b> If you have more cards in hand than each opponent, discard 2 cards from hand to make an opponent exert a minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("glamdring");

		assertEquals("Glamdring", card.getBlueprint().getTitle());
		assertEquals("Elven Blade", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnlyOnGandalfAndMakesHimDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glamdring = scn.GetFreepsCard("glamdring");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(avenger);
		scn.MoveCardsToHand(gandalf, glamdring);

		scn.StartGame();

		// Frodo and a [gandalf] companion of another title: nobody may bear it.
		assertInHand(glamdring);
		assertFalse(scn.FreepsPlayAvailable(glamdring));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(gandalf);
		assertTrue(scn.FreepsPlayAvailable(glamdring));
		assertEquals(0, scn.GetKeywordCount(gandalf, Keyword.DAMAGE));

		// Gandalf is the only bearer it may have, so nobody is asked.
		scn.FreepsPlayCard(glamdring);
		assertAttachedTo(glamdring, gandalf);
		assertEquals(1, scn.GetKeywordCount(gandalf, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(avenger, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithMoreCardsInHandSkirmishActionDiscardsTwoToMakeTheShadowPlayerExertAMinionOfHisChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glamdring = scn.GetFreepsCard("glamdring");
		var gandalf = scn.GetFreepsCard("gandalf");
		var legolas = scn.GetFreepsCard("legolas");
		var armor = scn.GetFreepsCard("armor");
		var deftness = scn.GetFreepsCard("deftness");
		var party = scn.GetShadowCard("party");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, glamdring);
		scn.MoveCardsToHand(legolas, armor, deftness);
		scn.MoveCardsToHand(scn.GetShadowCard("legolas"), scn.GetShadowCard("armor"));
		assertInHand(legolas, armor, deftness);

		scn.StartGame();
		assertAttachedTo(glamdring, gandalf);
		scn.MoveMinionsToTable(party, troop, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, party);
		// The Troop and the Runner are left unassigned.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gandalf);

		// 3 cards against 2.
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(0, scn.GetWoundsOn(party));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.FreepsActionAvailable(glamdring));
		scn.FreepsUseCardAction(glamdring);

		// Any 2 cards: a companion, a possession and an event are offered.
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, armor, deftness));
		assertEquals(2, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCards(legolas, armor);
		assertInDiscard(legolas, armor);
		assertInHand(deftness);
		assertEquals(1, scn.GetFreepsHandCount());

		// The Shadow player chooses: the minion in the skirmish and one of another culture and race outside it,
		// and not the Runner, who has no vitality to spare, nor a character who is no minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(party, troop));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(gandalf, scn.GetRingBearer()));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(troop);

		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(party));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishActionIsNotOfferedWithAsManyCardsInHandAsTheOpponent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glamdring = scn.GetFreepsCard("glamdring");
		var gandalf = scn.GetFreepsCard("gandalf");
		var party = scn.GetShadowCard("party");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, glamdring);
		scn.MoveCardsToHand(scn.GetFreepsCard("legolas"), scn.GetFreepsCard("armor"), scn.GetFreepsCard("deftness"));
		scn.MoveCardsToHand(scn.GetShadowCard("legolas"), scn.GetShadowCard("armor"), scn.GetShadowCard("deftness"));

		scn.StartGame();
		assertAttachedTo(glamdring, gandalf);
		scn.MoveMinionsToTable(party);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, party);
		scn.FreepsResolveSkirmish(gandalf);

		// 3 cards against 3: not more.
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetShadowHandCount());
		assertFalse(scn.FreepsActionAvailable(glamdring));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishActionIsNotOfferedWithOneCardInHandThoughTheOpponentHasNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glamdring = scn.GetFreepsCard("glamdring");
		var gandalf = scn.GetFreepsCard("gandalf");
		var deftness = scn.GetFreepsCard("deftness");
		var party = scn.GetShadowCard("party");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, glamdring);
		scn.MoveCardsToHand(deftness);

		scn.StartGame();
		assertAttachedTo(glamdring, gandalf);
		scn.MoveMinionsToTable(party);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, party);
		scn.FreepsResolveSkirmish(gandalf);

		// More cards than the opponent, but not the 2 to discard.
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetShadowHandCount());
		assertFalse(scn.FreepsActionAvailable(glamdring));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishActionIsOfferedInAnotherCompanionsSkirmishAndPaidForNothingWhenNoMinionCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glamdring = scn.GetFreepsCard("glamdring");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var armor = scn.GetFreepsCard("armor");
		var deftness = scn.GetFreepsCard("deftness");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.AttachCardsTo(gandalf, glamdring);
		scn.MoveCardsToHand(legolas, armor, deftness);
		scn.MoveCardsToHand(scn.GetShadowCard("legolas"), scn.GetShadowCard("armor"));
		assertInHand(legolas, armor, deftness);

		scn.StartGame();
		assertAttachedTo(glamdring, gandalf);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);

		// Gandalf is in no skirmish; the only minion has 1 vitality.
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.FreepsActionAvailable(glamdring));
		scn.FreepsUseCardAction(glamdring);
		scn.FreepsChooseCards(legolas, armor);

		// Paid, and nobody exerts.
		assertInDiscard(legolas, armor);
		assertEquals(1, scn.GetFreepsHandCount());
		assertInPlay(runner);
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
