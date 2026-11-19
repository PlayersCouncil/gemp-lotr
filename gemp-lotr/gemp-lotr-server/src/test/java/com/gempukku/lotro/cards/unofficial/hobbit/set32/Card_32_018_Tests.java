package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_018_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. The skirmishes of
	// turn 1 are at The Troll Hoard (site 2), where no ally is at home.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("weapon", "32_18");
					put("weapon2", "32_18");

					put("bard", "30_22");       // Bard, The Bowman: [esgaroth] Man ally, home 6H; a regroup ability not used
					put("betsy", "33_19");      // Betsy Butterbur: [gandalf] Man ally, home 1H; cannot take wounds, a fellowship ability not used
					put("boromir", "0_65");     // Boromir, Steward's Heir: [gondor] Man companion, keywords only (no Hobbit-set companion is a Man)
					put("thorin", "30_19");     // Thorin, Oakenshield: [dwarven] companion, strength 7; a fellowship ability not used
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion, strength 7; a fellowship ability not used
					put("dain", "32_2");        // Dain Ironfoot, Lord of the Iron Hills: [dwarven] ally, no Man; his regroup trigger not reached

					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc, strength 6, vitality 2; an assignment ability not used
					put("bats", "32_30");       // Dark Bats: [moria] Creature, strength 4, vitality 2; a Shadow ability not used
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	// Trollshaw Forest 30_59 as site 2: at the start of the maneuver phase each player may play a hand weapon from
	// his draw deck, which puts this weapon in play where minions stand.
	protected VirtualTableScenario GetTrollshawScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("weapon", "32_18");

					put("bard", "30_22");       // Bard, The Bowman: [esgaroth] Man ally; a regroup ability not used
					put("thorin", "30_19");     // Thorin, Oakenshield: [dwarven] companion; a fellowship ability not used

					put("braga", "33_25");      // Braga, Captain of the Guard: [men] Man minion; fierce, a skirmish ability not used
					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc, no Man; an assignment ability not used
				}},
				new HashMap<>()
				{{
					put("site1", "30_49");
					put("site2", "30_59");      // Trollshaw Forest
					put("site3", "30_51");
					put("site4", "30_52");
					put("site5", "30_53");
					put("site6", "30_54");
					put("site7", "30_55");
					put("site8", "30_56");
					put("site9", "30_57");
				}},
				null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void IronForgedWeaponStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Iron-Forged Weapon
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Esgaroth
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Hand Weapon
		 * Strength: +2
		 * Game Text: Bearer must be a Man or a [dwarven] companion.
		 * 	<b>Skirmish:</b> If bearer is not assigned to a skirmish, discard this possession to wound a minion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("weapon");

		assertEquals("Iron-Forged Weapon", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ESGAROTH, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnAManOrADwarvenCompanionAndOnNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var weapon = scn.GetFreepsCard("weapon");
		var bard = scn.GetFreepsCard("bard");
		var betsy = scn.GetFreepsCard("betsy");
		var boromir = scn.GetFreepsCard("boromir");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var dain = scn.GetFreepsCard("dain");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(boromir, thorin, balin);
		scn.MoveAlliesToTable(bard, betsy, dain);

		scn.StartGame();
		scn.MoveCardsToHand(weapon);
		scn.FreepsRefreshDecision();
		assertInHand(weapon);

		assertTrue(scn.FreepsPlayAvailable(weapon));
		scn.FreepsPlayCard(weapon);
		// Men of two cultures, allies and a companion; [dwarven] companions. Not Bilbo, a [shire] companion who is
		// no Man, nor Dain, a [dwarven] ally who is neither a Man nor a companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(bard, betsy, boromir, thorin, balin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bilbo, dain));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(thorin);

		assertAttachedTo(weapon, thorin);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayedWhereMinionsStandItMayGoOnTheOpponentsManMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTrollshawScenario();

		var weapon = scn.GetFreepsCard("weapon");
		var bard = scn.GetFreepsCard("bard");
		var thorin = scn.GetFreepsCard("thorin");
		var bilbo = scn.GetRingBearer();
		var braga = scn.GetShadowCard("braga");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCompanionsToTable(thorin);
		scn.MoveAlliesToTable(bard);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(braga, aggressor);
		assertInZone(Zone.DECK, weapon);

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsDecisionAvailable("hand weapon"));
		scn.FreepsChooseYes();
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(weapon);
		// "A Man" names no side: the Shadow player's Man minion may bear it, beside a Man ally and a [dwarven]
		// companion. Not an Orc minion, nor Bilbo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(braga, bard, thorin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aggressor, bilbo));
		scn.FreepsChooseCard(braga);

		assertAttachedTo(weapon, braga);
		// The Shadow player has no hand weapon he could play, so he is not asked (F-0800's reading: Trollshaw Forest
		// 30_59 asks only with one in the deck).
		assertFalse(scn.ShadowDecisionAvailable("hand weapon"));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void SkirmishActionDiscardsItFromAnUnassignedAllyToWoundAMinionInOrOutOfTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var weapon = scn.GetFreepsCard("weapon");
		var bard = scn.GetFreepsCard("bard");
		var thorin = scn.GetFreepsCard("thorin");
		var bilbo = scn.GetRingBearer();
		var aggressor = scn.GetShadowCard("aggressor");
		var bats = scn.GetShadowCard("bats");
		scn.MoveCompanionsToTable(thorin);
		scn.MoveAlliesToTable(bard);

		scn.StartGame();
		scn.AttachCardsTo(bard, weapon);
		assertAttachedTo(weapon, bard);
		scn.FreepsPass();
		scn.MoveMinionsToTable(aggressor, bats);
		// The Bats are exhausted: a wound kills them, where an exertion could not be made.
		scn.AddWoundsToChar(bats, 1);
		assertEquals(1, scn.GetWoundsOn(bats));
		assertEquals(0, scn.GetWoundsOn(aggressor));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thorin, aggressor);
		// The Bats are left out of every skirmish.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(thorin);

		// Bard, an ally away from home, is in no skirmish.
		assertFalse(scn.IsCharAssigned(bard));
		assertTrue(scn.FreepsActionAvailable(weapon));
		scn.FreepsUseCardAction(weapon);
		assertInZone(Zone.DISCARD, weapon);
		// A minion in the skirmish and one in none, of two cultures and races; no Free Peoples character.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aggressor, bats));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(thorin, bilbo, bard));
		scn.FreepsChooseCard(bats);

		assertInZone(Zone.DISCARD, bats);
		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWhileItsBearerIsAssignedThoughACopyOnAnUnassignedCompanionIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var weapon = scn.GetFreepsCard("weapon");
		var weapon2 = scn.GetFreepsCard("weapon2");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var bilbo = scn.GetRingBearer();
		var aggressor = scn.GetShadowCard("aggressor");
		var bats = scn.GetShadowCard("bats");
		scn.MoveCompanionsToTable(thorin, balin);

		scn.StartGame();
		scn.AttachCardsTo(thorin, weapon);
		scn.AttachCardsTo(balin, weapon2);
		assertAttachedTo(weapon, thorin);
		assertAttachedTo(weapon2, balin);
		scn.FreepsPass();
		scn.MoveMinionsToTable(aggressor, bats);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{bilbo, bats}, new PhysicalCardImpl[]{thorin, aggressor});
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// Bilbo's skirmish first: Thorin is assigned and waits for his own; Balin is in none.
		scn.FreepsResolveSkirmish(bilbo);
		assertTrue(scn.IsCharAssigned(thorin));
		assertFalse(scn.IsCharAssigned(balin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(weapon));
		assertTrue(scn.FreepsActionAvailable(weapon2));

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(thorin);
		assertTrue(scn.IsCharSkirmishingAgainst(thorin, aggressor));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(weapon));
		assertTrue(scn.FreepsActionAvailable(weapon2));
	}
}
