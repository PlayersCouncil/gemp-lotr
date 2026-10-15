package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_016_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("race", "2_16");
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf companion; a skirmish ability only
					put("orophin", "1_56");     // Orophin, Lorien Bowman: an Elf ally, played from hand; an archery ability only
					// Shoulder to Shoulder: an [elven] card that is no Elf; a maneuver ability only
					put("shoulder", "1_59");
					put("legolas", "1_50");     // Legolas, Greenleaf: Elf companion, strength 6, an archer
					// Stairs of Khazad-dum: another Free Peoples support condition, the control of the discard
					put("stairs", "1_24");
					// Cirith Ungol Guard: a [sauron] Uruk-hai, strength 9, Damage +1 its whole text
					put("guard", "10_81");
					// Saruman, Of Many Colours: an [isengard] minion who is no Uruk-hai, strength 8, Damage +1;
					// his naming of a culture comes only when he is played
					put("saruman", "12_54");
					// Uruk-hai Raiding Party: an [isengard] Uruk-hai, strength 9, vitality 3, Damage +1 its whole text
					put("party", "1_158");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetUrukHaiScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("race", "2_16");
					put("legolas", "1_50");     // Legolas, Greenleaf: the Elf to spot
					// Uruk Savage: an Uruk-hai of strength 5; Damage +1 is its whole text
					put("savage", "1_151");
					// Cave Troll of Moria: a Troll of strength 15 with damage +1 (and fierce); its other texts
					// are about playing it
					put("troll", "1_165");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ABlendedRaceStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: A Blended Race
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot an Elf. Plays to your support area.<br>Each Uruk-hai is strength -2 and loses all <b>damage</b> bonuses. Discard this condition during the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("race");

		assertEquals("A Blended Race", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachUrukHaiLosesItsDamageBonusAndAMinionOfAnotherRaceKeepsIts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetUrukHaiScenario();

		var race = scn.GetFreepsCard("race");
		var legolas = scn.GetFreepsCard("legolas");
		var savage = scn.GetShadowCard("savage");
		var troll = scn.GetShadowCard("troll");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToHand(race);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troll);
		scn.FreepsRefreshDecision();

		assertEquals(1, scn.GetKeywordCount(savage, Keyword.DAMAGE));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(1, scn.GetKeywordCount(troll, Keyword.DAMAGE));
		assertEquals(15, scn.GetStrength(troll));
		assertTrue(scn.FreepsPlayAvailable(race));

		scn.FreepsPlayCard(race);
		assertInZone(Zone.SUPPORT, race);
		// The Uruk-hai: no damage bonus, and strength 5 - 2
		assertFalse(scn.HasKeyword(savage, Keyword.DAMAGE));
		assertEquals(3, scn.GetStrength(savage));
		// The Troll is as it was
		assertEquals(1, scn.GetKeywordCount(troll, Keyword.DAMAGE));
		assertEquals(15, scn.GetStrength(troll));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
	@Test
	public void CannotBePlayedWithoutAnElfButCanOnceAnElfAllyIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var race = scn.GetFreepsCard("race");
		var gimli = scn.GetFreepsCard("gimli");
		var orophin = scn.GetFreepsCard("orophin");
		var shoulder = scn.GetFreepsCard("shoulder");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(shoulder);
		scn.MoveCardsToHand(race, orophin);

		scn.StartGame();

		// Frodo, a Dwarf and an [elven] condition: no Elf to spot.
		assertInZone(Zone.SUPPORT, shoulder);
		assertInHand(race, orophin);
		assertFalse(scn.FreepsPlayAvailable(race));

		scn.FreepsPlayCard(orophin);
		assertInZone(Zone.SUPPORT, orophin);
		assertTrue(scn.FreepsPlayAvailable(race));

		scn.FreepsPlayCard(race);
		assertInZone(Zone.SUPPORT, race);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnUrukHaiOfAnotherCultureIsWeakenedTooAndAnIsengardMinionWhoIsNoUrukHaiIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var race = scn.GetFreepsCard("race");
		var guard = scn.GetShadowCard("guard");
		var saruman = scn.GetShadowCard("saruman");

		scn.StartGame();
		scn.MoveMinionsToTable(guard, saruman);

		assertEquals(9, scn.GetStrength(guard));
		assertEquals(1, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(8, scn.GetStrength(saruman));
		assertEquals(1, scn.GetKeywordCount(saruman, Keyword.DAMAGE));

		scn.MoveCardsToSupportArea(race);

		// The [sauron] Uruk-hai: 9 - 2, and no damage bonus.
		assertEquals(7, scn.GetStrength(guard));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		// The [isengard] Wizard is as he was.
		assertEquals(8, scn.GetStrength(saruman));
		assertEquals(1, scn.GetKeywordCount(saruman, Keyword.DAMAGE));
	}

	@Test
	public void AnUrukHaiPlayedLaterIsWeakenedThroughTheSkirmishAndTheConditionIsDiscardedAsTheRegroupPhaseStarts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var race = scn.GetFreepsCard("race");
		var stairs = scn.GetFreepsCard("stairs");
		var legolas = scn.GetFreepsCard("legolas");
		var party = scn.GetShadowCard("party");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToSupportArea(race, stairs);
		scn.MoveCardsToHand(party);

		scn.StartGame();
		scn.SetTwilight(10);
		assertInZone(Zone.SUPPORT, race, stairs);
		assertInHand(party);

		scn.FreepsPass();
		scn.ShadowPlayCard(party);
		// 9 - 2, and no damage bonus, as he comes into play.
		assertEquals(7, scn.GetStrength(party));
		assertEquals(0, scn.GetKeywordCount(party, Keyword.DAMAGE));

		// Legolas's archery wound is placed on the one minion unasked.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(legolas, party);
		scn.FreepsResolveSkirmish(legolas);

		// Still in play during the skirmish, the phase before regroup.
		assertInZone(Zone.SUPPORT, race);
		assertEquals(7, scn.GetStrength(party));
		assertEquals(0, scn.GetKeywordCount(party, Keyword.DAMAGE));

		// 7 against Legolas's 6.
		scn.PassSkirmishActions();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInDiscard(race);
		assertInZone(Zone.SUPPORT, stairs);
		assertEquals(9, scn.GetStrength(party));
		assertEquals(1, scn.GetKeywordCount(party, Keyword.DAMAGE));
	}
}
