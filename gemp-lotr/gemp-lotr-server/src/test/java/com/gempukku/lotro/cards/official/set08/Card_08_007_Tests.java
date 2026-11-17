package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_007_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("unheard", "8_7");

					put("gimli", "1_13");       // Gimli, Son of Glóin: Dwarf companion, strength 6, damage +1; a skirmish ability used only on purpose
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally, strength 4, home site 3; a maneuver ability used only on purpose
					put("ring", "9_8");         // Ring of Guile: artifact on a Dwarf, strength +1, bearer is damage +1; a maneuver ability used only on purpose
					put("dwarfguard", "1_7");   // Dwarf Guard: Dwarf companion, strength 4, no damage bonus; "To play, spot a Dwarf"
					put("huorn", "9_25");       // Huorn: a [gandalf] companion, strength 6, who is damage +1 and no Dwarf; a response that needs an Ent
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability used only on purpose

					put("gollum", "5_24");      // Gollum, Nasty Treacherous Creature: [gollum] minion, strength 5, vitality 4; a regroup ability
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; "Damage +1." is its whole text
					put("warrior", "4_165");    // Orthanc Warrior: [isengard] Uruk-hai, strength 7, vitality 2; "Damage +1." is its whole text
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("scrabbler", "2_64");   // Goblin Scrabbler: [moria] Orc, strength 4, vitality 1; its texts are about playing minions
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, strength 8, vitality 1, no text
					put("whelp", "7_202");      // Morgul Whelp: [wraith] Orc, strength 3 (+6 only while the Ring-bearer is wounded), vitality 1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UnheardofStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Unheard of
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Response
		 * Game Text: If a Dwarf who is damage +X wins a skirmish, wound a minion not assigned to a skirmish X times.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("unheard");

		assertEquals("Unheard of", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ADwarfWhoIsDamagePlusTwoWinsAndAMinionNotAssignedIsWoundedTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var unheard = scn.GetFreepsCard("unheard");
		var gimli = scn.GetFreepsCard("gimli");
		var ring = scn.GetFreepsCard("ring");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.AttachCardsTo(gimli, ring);
		scn.MoveCardsToHand(unheard);

		var gollum = scn.GetShadowCard("gollum");
		var savage = scn.GetShadowCard("savage");
		var warrior = scn.GetShadowCard("warrior");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, savage, warrior, troop);
		scn.SkipToAssignments();
		// Gimli fights first; Aragorn's skirmish is still to come.  The Warrior and the Troop are assigned to nobody.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{gimli, gollum},
				new PhysicalCardImpl[]{aragorn, savage});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);

		// 6 + 1 for the Ring against Gollum's 5; damage +1 printed and +1 from the Ring.
		assertEquals(7, scn.GetStrength(gimli));
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(unheard));
		scn.PassSkirmishActions();

		// Gollum takes 1 wound and 2 for damage +2, and lives on one vitality.
		assertEquals(3, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(warrior));
		assertTrue(scn.FreepsPlayAvailable(unheard));
		scn.FreepsPlayCard(unheard);

		// Minions of two cultures and races assigned to nobody; not Gollum, still in the skirmish phase he was
		// assigned to, nor the Savage waiting for Aragorn, nor a companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(warrior, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gollum, savage, gimli, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(troop);

		// Two wounds on the one minion chosen; vitality 3, so he lives to show both.
		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(warrior));
		assertEquals(3, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertInDiscard(unheard);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void ADwarfAllyWhoIsDamagePlusOneWinsAndOneWoundKillsAnExhaustedMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var unheard = scn.GetFreepsCard("unheard");
		var thrarin = scn.GetFreepsCard("thrarin");
		var ring = scn.GetFreepsCard("ring");
		scn.MoveAlliesToTable(thrarin);
		scn.AttachCardsTo(thrarin, ring);
		scn.MoveCardsToHand(unheard);

		var scrabbler = scn.GetShadowCard("scrabbler");
		var warrior = scn.GetShadowCard("warrior");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		// Thrarin's home is site 3: he may be assigned there.
		scn.SkipToSite(2);
		scn.FreepsPass();
		scn.MoveMinionsToTable(scrabbler, warrior, troop);
		// Vitality 2 with a wound: the Warrior is exhausted, and could not be exerted.
		scn.AddWoundsToChar(warrior, 1);
		assertEquals(1, scn.GetWoundsOn(warrior));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, scrabbler);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(thrarin);
		// 4 + 1 for the Ring against the Scrabbler's 4.
		assertEquals(5, scn.GetStrength(thrarin));
		assertEquals(1, scn.GetKeywordCount(thrarin, Keyword.DAMAGE));
		scn.PassSkirmishActions();

		assertInDiscard(scrabbler);
		assertTrue(scn.FreepsPlayAvailable(unheard));
		scn.FreepsPlayCard(unheard);

		assertTrue(scn.FreepsHasCardChoiceAvailable(warrior, troop));
		scn.FreepsChooseCard(warrior);

		// One wound, X being 1: it kills the exhausted Warrior.  The Troop is untouched.
		assertInDiscard(warrior);
		assertEquals(0, scn.GetWoundsOn(troop));
		assertInDiscard(unheard);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenTheDwarfLosesTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var unheard = scn.GetFreepsCard("unheard");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(unheard);
		assertInHand(unheard);

		var brigand = scn.GetShadowCard("brigand");
		var warrior = scn.GetShadowCard("warrior");

		scn.StartGame();
		scn.MoveMinionsToTable(brigand, warrior);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, brigand);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.PassSkirmishActions();

		// The Brigand's 8 beats Gimli's 6: one wound, and he lives.  A minion assigned to nobody is there.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(warrior));
		assertInHand(unheard);
		assertFalse(scn.FreepsPlayAvailable(unheard));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenACompanionWhoIsNoDwarfWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var unheard = scn.GetFreepsCard("unheard");
		var huorn = scn.GetFreepsCard("huorn");
		scn.MoveCompanionsToTable(huorn);
		scn.MoveCardsToHand(unheard);
		assertInHand(unheard);

		var savage = scn.GetShadowCard("savage");
		var warrior = scn.GetShadowCard("warrior");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, warrior);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(huorn, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(huorn);
		// Huorn, 6 and damage +1, beats the Savage's 5 as Gimli would.
		assertEquals(6, scn.GetStrength(huorn));
		assertEquals(1, scn.GetKeywordCount(huorn, Keyword.DAMAGE));
		scn.PassSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(warrior));
		assertInHand(unheard);
		assertFalse(scn.FreepsPlayAvailable(unheard));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenADwarfWithNoDamageBonusWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var unheard = scn.GetFreepsCard("unheard");
		var dwarfguard = scn.GetFreepsCard("dwarfguard");
		scn.MoveCompanionsToTable(dwarfguard);
		scn.MoveCardsToHand(unheard);
		assertInHand(unheard);

		var whelp = scn.GetShadowCard("whelp");
		var warrior = scn.GetShadowCard("warrior");

		scn.StartGame();
		scn.MoveMinionsToTable(whelp, warrior);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(dwarfguard, whelp);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(dwarfguard);
		// The Guard's 4 beats the Whelp's 3 (the Ring-bearer is unwounded); the Guard has no damage bonus.
		assertEquals(4, scn.GetStrength(dwarfguard));
		assertEquals(3, scn.GetStrength(whelp));
		assertEquals(0, scn.GetKeywordCount(dwarfguard, Keyword.DAMAGE));
		scn.PassSkirmishActions();

		assertInDiscard(whelp);
		assertEquals(0, scn.GetWoundsOn(warrior));
		assertInHand(unheard);
		assertFalse(scn.FreepsPlayAvailable(unheard));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoMinionAssignedToNobodyItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var unheard = scn.GetFreepsCard("unheard");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(unheard);

		var gollum = scn.GetShadowCard("gollum");

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, gollum);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();

		// Gimli's 6 beats Gollum's 5: 1 wound and 1 for damage +1.  Gollum is the only minion, and is still
		// assigned in this skirmish phase: there is no minion to wound, and the event is played all the same
		// (CR 4.2 "effect").
		assertEquals(2, scn.GetWoundsOn(gollum));
		assertTrue(scn.FreepsPlayAvailable(unheard));
		scn.FreepsPlayCard(unheard);

		assertEquals(2, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertInDiscard(unheard);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
