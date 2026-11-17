package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_058_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer; the Shadow phase and
	// everything after it on turn 1 are at The Troll Hoard (site 2), whose only text is a start-of-turn offer, and
	// Tom's site is 2, so he is not roaming there.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tom", "31_58");

					// Minions to discard for the play, and not
					put("yazneg", "30_41");     // Yazneg, Orkish Assassin: [gundabad] Orc, cost 1; a maneuver ability not used
					put("malevolent", "33_47"); // Malevolent Orc: [sauron] Orc; an end-of-Shadow-phase offer with nothing to take
					put("ferny", "33_22");      // Bill Ferny Sr., Servant of Azog: a [gundabad] Man; texts on assigning him, a regroup ability
					// Cards in hand whose cost is read
					put("william", "31_61");    // William, Troll of Ettenmoors: [troll] Troll, cost 5; his texts work only in play
					put("demolition", "32_31"); // Demolition Troll: [moria] Troll, cost 5; a skirmish ability not used
					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc, cost 2; an assignment ability not used

					put("thorin", "30_19");     // Thorin, Oakenshield: [dwarven] Dwarf companion, strength 7, vitality 4
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: [dwarven] Dwarf ally, strength 4, vitality 2; "Maneuver: Exert Thrarin to allow him to participate in ... skirmishes"
					put("bats", "32_30");       // Dark Bats: [moria] Creature, strength 4, vitality 2; a Shadow ability not used
					put("spider", "31_55");     // Wicked Spider: [spider] Spider, fierce, strength 7; a skirmish ability not used
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] Dwarf companion, strength 7, vitality 3; a fellowship ability not used
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void TomStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: Tom, Troll of Ettenmoors
		 * Unique: True
		 * Side: Shadow
		 * Culture: Troll
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Troll
		 * Strength: 11
		 * Vitality: 4
		 * Site Number: 2
		 * Game Text: <b>Fierce</b>. To play, discard an Orc.
		 * The twilight cost of each Troll is -2.
		 * Each time a companion loses a skirmish, each Troll is strength +1 until the regroup phase.
		 */

		var scn = GetScenario();

		var card = scn.GetShadowCard("tom");

		assertEquals("Tom", card.getBlueprint().getTitle());
		assertEquals("Troll of Ettenmoors", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.TROLL, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.TROLL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(2, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayingTomDiscardsAnOrcAndCostsHisFullFiveThenEachTrollInHandCostsTwoLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tom = scn.GetShadowCard("tom");
		var yazneg = scn.GetShadowCard("yazneg");
		var malevolent = scn.GetShadowCard("malevolent");
		var ferny = scn.GetShadowCard("ferny");
		var william = scn.GetShadowCard("william");
		var demolition = scn.GetShadowCard("demolition");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCardsToHand(tom, william, demolition, aggressor);

		scn.StartGame();
		assertInZone(Zone.HAND, tom);
		scn.SetTwilight(10);
		scn.MoveMinionsToTable(yazneg, malevolent, ferny);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(5, scn.GetTwilightCost(william));
		assertEquals(5, scn.GetTwilightCost(demolition));
		assertEquals(2, scn.GetTwilightCost(aggressor));
		int pool = scn.GetTwilight();
		assertTrue(scn.ShadowPlayAvailable(tom));
		scn.ShadowPlayCard(tom);
		assertTrue(scn.ShadowHasCardChoiceAvailable(yazneg, malevolent));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ferny));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(yazneg);
		assertInZone(Zone.DISCARD, yazneg);
		assertInZone(Zone.SHADOW_CHARACTERS, malevolent);
		assertInZone(Zone.SHADOW_CHARACTERS, ferny);
		assertInZone(Zone.SHADOW_CHARACTERS, tom);
		// "Each Troll" does not name Tom, so it does not lower his own cost as he is played: 5, not 3.
		assertEquals(pool - 5, scn.GetTwilight());
		// With Tom in play: 5 - 2 for each Troll, of either culture; the Orc is not lowered.
		assertEquals(3, scn.GetTwilightCost(william));
		assertEquals(3, scn.GetTwilightCost(demolition));
		assertEquals(2, scn.GetTwilightCost(aggressor));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TomCannotBePlayedUntilAnOrcIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tom = scn.GetShadowCard("tom");
		var yazneg = scn.GetShadowCard("yazneg");
		var ferny = scn.GetShadowCard("ferny");
		scn.MoveCardsToHand(tom, yazneg);

		scn.StartGame();
		assertInZone(Zone.HAND, tom);
		assertInZone(Zone.HAND, yazneg);
		scn.SetTwilight(10);
		scn.MoveMinionsToTable(ferny);
		scn.FreepsPass();

		// Bill Ferny Sr. is no Orc, and the Orc in hand is not in play.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(tom));
		scn.ShadowPlayCard(yazneg);
		assertInZone(Zone.SHADOW_CHARACTERS, yazneg);
		assertTrue(scn.ShadowPlayAvailable(tom));
		scn.ShadowPlayCard(tom);
		// The one Orc is discarded with nothing asked.
		assertInZone(Zone.DISCARD, yazneg);
		assertInZone(Zone.SHADOW_CHARACTERS, ferny);
		assertInZone(Zone.SHADOW_CHARACTERS, tom);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachTimeACompanionLosesASkirmishEachTrollIsStrengthPlusOneUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tom = scn.GetShadowCard("tom");
		var demolition = scn.GetShadowCard("demolition");
		var yazneg = scn.GetShadowCard("yazneg");
		var bats = scn.GetShadowCard("bats");
		var spider = scn.GetShadowCard("spider");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin, balin);

		scn.StartGame();
		scn.MoveMinionsToTable(tom, demolition, yazneg, bats, spider);
		assertEquals(11, scn.GetStrength(tom));
		assertEquals(10, scn.GetStrength(demolition));
		assertEquals(5, scn.GetStrength(yazneg));
		assertEquals(4, scn.GetStrength(bats));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{thorin, tom}, new PhysicalCardImpl[]{bilbo, bats});
		// The Troll, the Orc and the Spider are left unassigned.
		scn.ShadowDeclineAssignments();

		// Thorin (7) loses to Tom (11) and takes a wound.
		scn.FreepsResolveSkirmish(thorin);
		scn.PassSkirmishActions();
		// The loss's wound and Tom's required trigger are ordered by the Free Peoples player.
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(thorin));
		assertEquals(12, scn.GetStrength(tom));
		assertEquals(11, scn.GetStrength(demolition));
		assertEquals(5, scn.GetStrength(yazneg));
		assertEquals(4, scn.GetStrength(bats));

		// Bilbo (3) loses to Dark Bats (4): a second companion, of another culture and race.
		scn.FreepsResolveSkirmish(bilbo);
		assertEquals(12, scn.GetStrength(tom));
		assertEquals(11, scn.GetStrength(demolition));
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertEquals(13, scn.GetStrength(tom));
		assertEquals(12, scn.GetStrength(demolition));
		assertEquals(5, scn.GetStrength(yazneg));
		assertEquals(4, scn.GetStrength(bats));

		// Tom and the Spider are fierce. Thorin loses to Tom again, the same companion a second time.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{thorin, tom}, new PhysicalCardImpl[]{balin, spider});
		scn.FreepsResolveSkirmish(thorin);
		assertEquals(13, scn.GetStrength(tom));
		scn.PassFierceSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.GetWoundsOn(thorin));
		assertEquals(14, scn.GetStrength(tom));
		assertEquals(13, scn.GetStrength(demolition));
		assertEquals(5, scn.GetStrength(yazneg));

		// Still in force in the last fierce skirmish; Balin (7) loses to the Spider (7) on the tie, and with that
		// skirmish over the regroup phase begins and the bonus is gone.
		scn.FreepsResolveSkirmish(balin);
		assertEquals(14, scn.GetStrength(tom));
		assertEquals(13, scn.GetStrength(demolition));
		scn.PassFierceSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(balin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(11, scn.GetStrength(tom));
		assertEquals(10, scn.GetStrength(demolition));
		assertEquals(5, scn.GetStrength(yazneg));
	}

	@Test
	public void ACompanionWinningOrAnAllyLosingMakesNoTrollStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tom = scn.GetShadowCard("tom");
		var yazneg = scn.GetShadowCard("yazneg");
		var bats = scn.GetShadowCard("bats");
		var thorin = scn.GetFreepsCard("thorin");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(thorin);

		scn.StartGame();
		scn.MoveAlliesToTable(thrarin);
		scn.MoveMinionsToTable(tom, yazneg, bats);
		assertEquals(11, scn.GetStrength(tom));
		assertEquals(0, scn.GetWoundsOn(thrarin));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{thorin, yazneg}, new PhysicalCardImpl[]{thrarin, bats});
		scn.ShadowDeclineAssignments();

		// Thorin (7) beats Yazneg (5).
		scn.FreepsResolveSkirmish(thorin);
		scn.PassSkirmishActions();
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertEquals(11, scn.GetStrength(tom));

		// Thrarin (4) loses to Dark Bats (4) on the tie, and his second wound kills him: he is an ally, no companion.
		scn.FreepsResolveSkirmish(thrarin);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, thrarin);
		assertEquals(11, scn.GetStrength(tom));
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
