package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_123_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("captain", "17_123");
					put("captain2", "17_123");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] companion, strength 8, vitality 4; no hunter
					// Faramir, Captain of Ithilien: [gondor] companion, strength 7, ranger and hunter 2; a maneuver heal only
					put("faramir", "18_48");
					put("tracking", "15_171");  // Tracking Uruk: [uruk-hai] Uruk-hai, strength 14; hunter 1 is its whole text
					// Hunting Herdsman: [men] Man, strength 8, hunter 1; +3 for each follower, and none is in play
					put("herdsman", "15_83");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, no text at all, so no hunter
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WhiteHandCaptainStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: White Hand Captain
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 12
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Fierce</b>.<br>While this minion is assigned to a skirmish, each hunter character is strength +1.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("captain");

		assertEquals("White Hand Captain", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhileItIsAssignedToASkirmishEachHunterCharacterIsStrengthPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var faramir = scn.GetFreepsCard("faramir");
		scn.MoveCompanionsToTable(aragorn, faramir);

		var captain = scn.GetShadowCard("captain");
		var tracking = scn.GetShadowCard("tracking");
		var herdsman = scn.GetShadowCard("herdsman");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(captain, tracking, herdsman, troop);
		scn.SkipToAssignments();

		// The Captain is in play and not assigned: every strength is as printed.  No skirmish is under way, so no
		// hunter has his own bonus.
		assertFalse(scn.IsCharAssigned(captain));
		assertEquals(7, scn.GetStrength(faramir));
		assertEquals(14, scn.GetStrength(tracking));
		assertEquals(8, scn.GetStrength(herdsman));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(12, scn.GetStrength(captain));

		scn.FreepsAssignToMinions(aragorn, captain);
		scn.ShadowDeclineAssignments();
		assertTrue(scn.IsCharAssignedAgainst(aragorn, captain));

		// A hunter companion and hunter minions of two cultures and races, on both sides, are +1; the characters
		// who are no hunters, the Captain among them, are not.
		assertEquals(8, scn.GetStrength(faramir));
		assertEquals(15, scn.GetStrength(tracking));
		assertEquals(9, scn.GetStrength(herdsman));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(12, scn.GetStrength(captain));

		// In its own skirmish it is still assigned.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(8, scn.GetStrength(faramir));
		assertEquals(15, scn.GetStrength(tracking));

		// 8 against 12: Aragorn loses and takes a wound, and lives.
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.SHADOW_CHARACTERS, captain);

		// Its skirmish is over and it is assigned to none: the bonus is gone.
		assertFalse(scn.IsCharAssigned(captain));
		assertEquals(7, scn.GetStrength(faramir));
		assertEquals(14, scn.GetStrength(tracking));
		assertEquals(8, scn.GetStrength(herdsman));
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AnotherMinionOrCopyAssignedGivesNothingUntilTheCaptainItselfIsAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var faramir = scn.GetFreepsCard("faramir");
		scn.MoveCompanionsToTable(aragorn, faramir);

		var captain = scn.GetShadowCard("captain");
		var tracking = scn.GetShadowCard("tracking");
		var troop = scn.GetShadowCard("troop");
		var captain2 = scn.GetShadowCard("captain2");

		scn.StartGame();
		scn.MoveMinionsToTable(captain, captain2, tracking, troop);
		scn.SkipToAssignments();

		assertEquals(7, scn.GetStrength(faramir));
		assertEquals(14, scn.GetStrength(tracking));

		// The Free Peoples player assigns the Troop and leaves the Captain: another minion assigned gives nothing.
		scn.FreepsAssignToMinions(aragorn, troop);
		assertTrue(scn.IsCharAssigned(troop));
		assertFalse(scn.IsCharAssigned(captain));
		assertEquals(7, scn.GetStrength(faramir));
		assertEquals(14, scn.GetStrength(tracking));

		// The Shadow player assigns one Captain and leaves the other: the one assigned gives +1, and the copy that
		// is not assigned gives nothing more.
		scn.ShadowAssignToMinions(faramir, captain);
		assertTrue(scn.IsCharAssignedAgainst(faramir, captain));
		assertFalse(scn.IsCharAssigned(captain2));
		assertEquals(8, scn.GetStrength(faramir));
		assertEquals(15, scn.GetStrength(tracking));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}
}
