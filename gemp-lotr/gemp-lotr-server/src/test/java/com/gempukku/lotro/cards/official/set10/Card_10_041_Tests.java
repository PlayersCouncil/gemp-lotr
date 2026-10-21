package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_041_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pillager", "10_41");
					// Easterling Attacker: a [raider] Easterling Man; "Skirmish: Exert this minion to make him damage +1",
					// not used
					put("attacker", "7_141");
					// Ulaire Attea, Khamul of the East: a [raider] Easterling who is a Nazgul and no Man; his text
					// needs 3 burdens
					put("khamul", "103_61");
					// Southron Explorer: a [raider] Southron Man, no Easterling; a regroup ability only
					put("explorer", "4_250");

					// Six companions besides Frodo, of four cultures and four races
					put("sam", "1_311");        // Sam, Son of Hamfast: fellowship and response abilities only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability not used
					put("gimli", "1_13");       // Gimli, Son of Gloin: a skirmish ability only
					put("boromir", "1_97");     // Boromir, Son of Denethor: a skirmish ability only
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: his text is his own strength
					put("knight", "5_35");      // Gondorian Knight: Knight is his whole text
					// Elrond, Lord of Rivendell: an ally, a character who is no companion; his start-of-turn heal and
					// fellowship draw do nothing here
					put("elrond", "1_40");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EasterlingPillagerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Easterling Pillager
		 * Unique: True
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 11
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Easterling</b>. <b>Fierce</b>.<br>When you play this minion, you may spot another Easterling to add a burden for each companion over 5.
		*/

		var scn = GetScenario();

		var pillager = scn.GetFreepsCard("pillager");

		assertEquals("Easterling Pillager", pillager.getBlueprint().getTitle());
		assertNull(pillager.getBlueprint().getSubtitle());
		assertTrue(pillager.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, pillager.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, pillager.getBlueprint().getCulture());
		assertEquals(CardType.MINION, pillager.getBlueprint().getCardType());
		assertEquals(Race.MAN, pillager.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(pillager, Keyword.EASTERLING));
		assertTrue(scn.HasKeyword(pillager, Keyword.FIERCE));
		assertEquals(5, pillager.getBlueprint().getTwilightCost());
		assertEquals(11, pillager.getBlueprint().getStrength());
		assertEquals(3, pillager.getBlueprint().getVitality());
		assertEquals(4, pillager.getBlueprint().getSiteNumber());
	}

	// Ketura on Q-0034: an optional action with no cost that would do nothing is not offered (a spot is no
	// cost).  "A burden for each companion over 5" is no burden with five companions.
	@Test
	public void TheBurdenIsNotOfferedWithFiveCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// Frodo and four companions, and an ally, who is no companion
		scn.MoveCompanionsToTable(scn.GetFreepsCard("sam"), scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("gimli"),
				scn.GetFreepsCard("boromir"));
		scn.MoveAlliesToTable(scn.GetFreepsCard("elrond"));

		var pillager = scn.GetShadowCard("pillager");
		var attacker = scn.GetShadowCard("attacker");
		scn.MoveCardsToHand(pillager);

		scn.StartGame();
		scn.MoveMinionsToTable(attacker);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// Another Easterling is there to spot.
		scn.ShadowPlayCard(pillager);
		assertInZone(Zone.SHADOW_CHARACTERS, pillager);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(pillager));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheBurdenIsOfferedWithSixCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// Frodo and five companions, and an ally, who is no companion
		scn.MoveCompanionsToTable(scn.GetFreepsCard("sam"), scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("gimli"),
				scn.GetFreepsCard("boromir"), scn.GetFreepsCard("gandalf"));
		scn.MoveAlliesToTable(scn.GetFreepsCard("elrond"));

		var pillager = scn.GetShadowCard("pillager");
		var attacker = scn.GetShadowCard("attacker");
		scn.MoveCardsToHand(pillager);

		scn.StartGame();
		scn.MoveMinionsToTable(attacker);
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(pillager);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(pillager));
		assertEquals(0, scn.GetBurdens());
		scn.ShadowAcceptOptionalTrigger();

		// 6 companions - 5: one burden, the companions counted with nothing asked (CR 4.2 "for each").
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithSevenCompanionsTwoBurdensAreAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable(scn.GetFreepsCard("sam"), scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("gimli"),
				scn.GetFreepsCard("boromir"), scn.GetFreepsCard("gandalf"), scn.GetFreepsCard("knight"));
		scn.MoveAlliesToTable(scn.GetFreepsCard("elrond"));

		var pillager = scn.GetShadowCard("pillager");
		var attacker = scn.GetShadowCard("attacker");
		scn.MoveCardsToHand(pillager);

		scn.StartGame();
		scn.MoveMinionsToTable(attacker);
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(pillager);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(pillager));
		assertEquals(0, scn.GetBurdens());
		scn.ShadowAcceptOptionalTrigger();

		// 7 companions - 5; the ally and the two minions are not counted.
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SpottingAnEasterlingWhoIsNoManItIsOfferedAndDeclinedAddsNoBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable(scn.GetFreepsCard("sam"), scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("gimli"),
				scn.GetFreepsCard("boromir"), scn.GetFreepsCard("gandalf"), scn.GetFreepsCard("knight"));

		var pillager = scn.GetShadowCard("pillager");
		var khamul = scn.GetShadowCard("khamul");
		scn.MoveCardsToHand(pillager);

		scn.StartGame();
		scn.MoveMinionsToTable(khamul);
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(pillager);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(pillager));
		assertEquals(0, scn.GetBurdens());
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.SHADOW_CHARACTERS, pillager);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithOnlyASouthronBesideItTheBurdenIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable(scn.GetFreepsCard("sam"), scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("gimli"),
				scn.GetFreepsCard("boromir"), scn.GetFreepsCard("gandalf"), scn.GetFreepsCard("knight"));

		var pillager = scn.GetShadowCard("pillager");
		var explorer = scn.GetShadowCard("explorer");
		scn.MoveCardsToHand(pillager);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// The Pillager is an Easterling himself, and a [raider] Man who is no Easterling is not another one.
		scn.ShadowPlayCard(pillager);
		assertInZone(Zone.SHADOW_CHARACTERS, pillager);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(pillager));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnotherEasterlingPlayedAfterItSetsOffNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable(scn.GetFreepsCard("sam"), scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("gimli"),
				scn.GetFreepsCard("boromir"), scn.GetFreepsCard("gandalf"), scn.GetFreepsCard("knight"));

		var pillager = scn.GetShadowCard("pillager");
		var attacker = scn.GetShadowCard("attacker");
		scn.MoveCardsToHand(attacker);

		scn.StartGame();
		scn.MoveMinionsToTable(pillager);
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(attacker);
		assertInZone(Zone.SHADOW_CHARACTERS, attacker);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(pillager));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
