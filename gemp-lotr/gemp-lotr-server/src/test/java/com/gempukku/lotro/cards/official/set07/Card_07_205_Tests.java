package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_205_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("strength", "7_205");
					put("lemenya", "1_232");    // Úlairë Lemenya: a [wraith] Nazgûl; her text is about [wraith] conditions
					// Ulaire Attea, Khamul of the East: a Nazgûl who is [raider]; strength +3 to each Easterling at 3
					// burdens
					put("khamul", "103_61");
					// Morgul Hound: a [wraith] Orc and no Nazgûl; +4 and fierce only at 2 wounds on the Ring-bearer
					put("hound", "7_192");
					// Two companions beside Frodo, to hold 3 threats
					put("aragorn", "1_89");     // Aragorn, Ranger of the North
					put("sam", "1_311");        // Sam, Son of Hamfast
					// Characters for the dead pile: Dwarf Guard, a companion with no text but its requirement to
					// play, and Bounder, a [shire] ally
					put("guard1", "1_7");
					put("guard2", "1_7");
					put("guard3", "1_7");
					put("bounder", "1_286");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetCorruptionScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("strength", "7_205");
					put("lemenya", "1_232");    // Úlairë Lemenya: a Nazgûl; her text is about [wraith] conditions
					// Three companions in play, the Ring-bearer among them, hold 3 threats.
					put("aragorn", "1_89");     // Aragorn, Ranger of the North
					put("boromir", "1_96");     // Boromir, Lord of Gondor
					// Three characters for the dead pile: two companions and an ally.
					put("gimli", "1_13");       // Gimli, Son of Glóin
					put("legolas", "1_50");     // Legolas, Greenleaf
					put("elrond", "1_40");      // Elrond, Lord of Rivendell
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PutForthHisStrengthStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Put Forth His Strength
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot a Nazgûl.<br><b>Shadow:</b> If you have initiative and there are 3 characters in the dead pile, spot 3 burdens, 3 threats and a Nazgûl to corrupt the Ring-bearer.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("strength");

		assertEquals("Put Forth His Strength", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithInitiativeThreeDeadCharactersThreeBurdensThreeThreatsAndANazgulTheRingBearerIsCorrupted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetCorruptionScenario();

		var strength = scn.GetShadowCard("strength");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("boromir"));
		scn.MoveCardsToDeadPile(scn.GetFreepsCard("gimli"), scn.GetFreepsCard("legolas"), scn.GetFreepsCard("elrond"));
		scn.MoveCardsToSupportArea(strength);

		scn.StartGame();
		scn.AddBurdens(3);
		scn.AddThreats(3);
		scn.MoveMinionsToTable(lemenya);
		assertEquals(3, scn.GetBurdens());
		assertEquals(3, scn.GetThreats());
		scn.FreepsPass();

		// Exactly 3 of each, and a Free Peoples hand of fewer than 4 cards, so the Shadow player has initiative.
		assertEquals(3, scn.GetFreepsDeadCount());
		assertEquals(3, scn.GetBurdens());
		assertEquals(3, scn.GetThreats());
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.ShadowActionAvailable(strength));

		scn.MarkGameLog();
		scn.ShadowUseCardAction(strength);
		assertTrue(scn.GameIsFinished());
		assertTrue(scn.ShadowHasWon());
		assertTrue(scn.GameLogSinceMarkSays(scn.P1() + " lost due to: The Ring-Bearer is corrupted"));
	}

	@Test
	public void CanBePlayedOnlyOnceANazgulCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strength = scn.GetShadowCard("strength");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToHand(strength, lemenya);
		assertInHand(strength, lemenya);

		scn.StartGame();
		scn.MoveMinionsToTable(hound);
		// 4 for Lemenya + 2 roaming at site 2, and 1 for this condition
		scn.SetTwilight(7);
		scn.FreepsPass();

		// A [wraith] Orc is no Nazgûl.
		assertInPlay(hound);
		assertFalse(scn.ShadowPlayAvailable(strength));

		scn.ShadowPlayCard(lemenya);
		assertInPlay(lemenya);
		assertTrue(scn.ShadowPlayAvailable(strength));
		scn.ShadowPlayCard(strength);
		assertInZone(Zone.SUPPORT, strength);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ANazgulOfAnotherCultureIsSpottedToPlayIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strength = scn.GetShadowCard("strength");
		var khamul = scn.GetShadowCard("khamul");
		scn.MoveCardsToHand(strength);
		assertInHand(strength);

		scn.StartGame();
		scn.MoveMinionsToTable(khamul);
		scn.SetTwilight(1);
		scn.FreepsPass();

		assertInPlay(khamul);
		assertTrue(scn.ShadowPlayAvailable(strength));
		scn.ShadowPlayCard(strength);
		assertInZone(Zone.SUPPORT, strength);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AbilityIsOfferedWithThreeCardsInTheFreePeoplesHandAndNotWithFour() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strength = scn.GetShadowCard("strength");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("sam"));
		scn.MoveCardsToDeadPile(scn.GetFreepsCard("guard1"), scn.GetFreepsCard("guard2"), scn.GetFreepsCard("bounder"));
		scn.MoveCardsToSupportArea(strength);
		// Free Peoples hand cards, here only for the count
		scn.MoveCardsToHand(scn.GetFreepsCard("strength"), scn.GetFreepsCard("lemenya"), scn.GetFreepsCard("khamul"));

		scn.StartGame();
		scn.AddBurdens(3);
		scn.AddThreats(3);
		scn.MoveMinionsToTable(lemenya);
		assertEquals(3, scn.GetFreepsDeadCount());
		assertEquals(3, scn.GetBurdens());
		assertEquals(3, scn.GetThreats());
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(strength));

		// A fourth card gives the Free Peoples player initiative.
		scn.MoveCardsToHand(scn.GetFreepsCard("hound"));
		scn.ShadowRefreshDecision();
		assertEquals(4, scn.GetFreepsHandCount());
		assertFalse(scn.ShadowHasInitiative());
		assertFalse(scn.ShadowActionAvailable(strength));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.GameIsFinished());
	}

	@Test
	public void AbilityIsNotOfferedWithTwoCharactersInTheFreePeoplesDeadPileAndOneInTheShadowPlayers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strength = scn.GetShadowCard("strength");
		var lemenya = scn.GetShadowCard("lemenya");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("sam"));
		scn.MoveCardsToDeadPile(scn.GetFreepsCard("guard1"), scn.GetFreepsCard("guard2"));
		// The Shadow player's own Dwarf Guard goes to his own dead pile, which is not "the dead pile" (Q-0131).
		scn.MoveCardsToDeadPile(scn.GetShadowCard("guard3"));
		scn.MoveCardsToSupportArea(strength);

		scn.StartGame();
		scn.AddBurdens(3);
		scn.AddThreats(3);
		scn.MoveMinionsToTable(lemenya);
		assertEquals(2, scn.GetFreepsDeadCount());
		assertInZone(Zone.DEAD, scn.GetShadowCard("guard3"));
		assertEquals(3, scn.GetBurdens());
		assertEquals(3, scn.GetThreats());
		assertTrue(scn.ShadowHasInitiative());
		scn.FreepsPass();

		assertFalse(scn.ShadowActionAvailable(strength));

		// An ally is a character: the third.
		scn.MoveCardsToDeadPile(bounder);
		scn.ShadowRefreshDecision();
		assertEquals(3, scn.GetFreepsDeadCount());
		assertTrue(scn.ShadowActionAvailable(strength));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AbilityIsNotOfferedWithTwoBurdensAndIsWithThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strength = scn.GetShadowCard("strength");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("sam"));
		scn.MoveCardsToDeadPile(scn.GetFreepsCard("guard1"), scn.GetFreepsCard("guard2"), scn.GetFreepsCard("bounder"));
		scn.MoveCardsToSupportArea(strength);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.AddThreats(3);
		scn.MoveMinionsToTable(lemenya);
		assertEquals(3, scn.GetFreepsDeadCount());
		assertEquals(2, scn.GetBurdens());
		assertEquals(3, scn.GetThreats());
		assertTrue(scn.ShadowHasInitiative());
		scn.FreepsPass();

		assertFalse(scn.ShadowActionAvailable(strength));

		scn.AddBurdens(1);
		scn.ShadowRefreshDecision();
		assertEquals(3, scn.GetBurdens());
		assertTrue(scn.ShadowActionAvailable(strength));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AbilityIsNotOfferedWithTwoThreatsAndIsWithThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strength = scn.GetShadowCard("strength");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("sam"));
		scn.MoveCardsToDeadPile(scn.GetFreepsCard("guard1"), scn.GetFreepsCard("guard2"), scn.GetFreepsCard("bounder"));
		scn.MoveCardsToSupportArea(strength);

		scn.StartGame();
		scn.AddBurdens(3);
		scn.AddThreats(2);
		scn.MoveMinionsToTable(lemenya);
		assertEquals(3, scn.GetFreepsDeadCount());
		assertEquals(3, scn.GetBurdens());
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.ShadowHasInitiative());
		scn.FreepsPass();

		assertFalse(scn.ShadowActionAvailable(strength));

		scn.AddThreats(1);
		scn.ShadowRefreshDecision();
		assertEquals(3, scn.GetThreats());
		assertTrue(scn.ShadowActionAvailable(strength));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AbilityIsNotOfferedWithAWraithOrcAndNoNazgulAndIsWithANazgulOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strength = scn.GetShadowCard("strength");
		var hound = scn.GetShadowCard("hound");
		var khamul = scn.GetShadowCard("khamul");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("sam"));
		scn.MoveCardsToDeadPile(scn.GetFreepsCard("guard1"), scn.GetFreepsCard("guard2"), scn.GetFreepsCard("bounder"));
		scn.MoveCardsToSupportArea(strength);

		scn.StartGame();
		scn.AddBurdens(3);
		scn.AddThreats(3);
		scn.MoveMinionsToTable(hound);
		assertEquals(3, scn.GetFreepsDeadCount());
		assertEquals(3, scn.GetBurdens());
		assertEquals(3, scn.GetThreats());
		assertTrue(scn.ShadowHasInitiative());
		assertInPlay(hound);
		scn.FreepsPass();

		assertFalse(scn.ShadowActionAvailable(strength));

		scn.MoveMinionsToTable(khamul);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(strength));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
