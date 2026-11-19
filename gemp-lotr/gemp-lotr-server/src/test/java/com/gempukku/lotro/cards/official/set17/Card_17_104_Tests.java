package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;
import java.util.HashMap;
import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_17_104_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("warrior", "17_104");
					put("warrior2", "17_104");

					put("guard", "5_83");       // Household Guard: [rohan] Man companion; Valiant is its whole text
					put("sword", "4_291");      // Sword of Rohan: [rohan] hand weapon, "Bearer must be a [rohan] Man"; damage +1 against an Uruk-hai only
					put("armor", "1_92");       // Armor: [gondor] armor, "Bearer must be a Man"; a wound cap in skirmishes
					put("shield", "5_91");      // Rohirrim Shield: [rohan] shield, "Bearer must be a [rohan] Man"; its regroup ability is used only on purpose
					// Brought Down from Inside: [rohan] condition, "Bearer must be a [rohan] Man"; at the start of each
					// skirmish, may be transferred to another [rohan] Man
					put("brought", "19_24");
					// Protecting the Hall: [rohan] condition, "Bearer must be a [rohan] Man" (toil 2); its assignment
					// ability is used only on purpose
					put("hall", "11_149");
					put("pipe", "1_285");       // Bilbo's Pipe: [shire] pipe, "Bearer must be a Hobbit"; its fellowship ability is used only on purpose

					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; adds (2) on its own play only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WarriorofRohanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Warrior of Rohan
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: Possessions cannot be transferred from this companion to another character.<br><b>Fellowship:</b> Play a possession on this companion to liberate a site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("warrior");

		assertEquals("Warrior of Rohan", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void PossessionsHeBearsCannotBeTransferredButAPossessionMayBeTransferredToHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior = scn.GetFreepsCard("warrior");
		var guard = scn.GetFreepsCard("guard");
		var sword = scn.GetFreepsCard("sword");
		var armor = scn.GetFreepsCard("armor");
		var shield = scn.GetFreepsCard("shield");
		scn.MoveCompanionsToTable(warrior, guard);
		scn.AttachCardsTo(warrior, sword, armor);
		scn.AttachCardsTo(guard, shield);
		assertAttachedTo(sword, warrior);
		assertAttachedTo(armor, warrior);
		assertAttachedTo(shield, guard);

		scn.StartGame();

		// The Guard bears no hand weapon and no armor, and could bear either.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsTransferAvailable(sword));
		assertFalse(scn.FreepsTransferAvailable(armor));

		// The Shield on the Guard may be transferred, and the Warrior is the one other [rohan] Man to take it.
		assertTrue(scn.FreepsTransferAvailable(shield));
		scn.FreepsTransferCard(shield);
		assertAttachedTo(shield, warrior);
		assertAttachedTo(sword, warrior);
		assertAttachedTo(armor, warrior);

		// Now on him, it cannot leave him again.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsTransferAvailable(shield));
	}

	@Test
	public void AConditionHeBearsIsNoPossessionAndIsStillTransferredFromHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior = scn.GetFreepsCard("warrior");
		var guard = scn.GetFreepsCard("guard");
		var brought = scn.GetFreepsCard("brought");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(warrior, guard);
		scn.AttachCardsTo(warrior, brought);
		assertAttachedTo(brought, warrior);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(warrior, runner);
		scn.FreepsResolveSkirmish(warrior);

		assertAttachedTo(brought, warrior);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(brought));
		scn.FreepsAcceptOptionalTrigger();

		// The Guard is the one [rohan] Man who does not bear it.
		assertAttachedTo(brought, guard);
		assertNotAttachedTo(brought, warrior);
		assertTrue(scn.IsCharSkirmishingAgainst(warrior, runner));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void FellowshipActionPlaysAPossessionOnThisCompanionToLiberateTheHighestSiteTheOpponentControls() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior = scn.GetFreepsCard("warrior");
		var warrior2 = scn.GetFreepsCard("warrior2");
		var sword = scn.GetFreepsCard("sword");
		var armor = scn.GetFreepsCard("armor");
		var hall = scn.GetFreepsCard("hall");
		var pipe = scn.GetFreepsCard("pipe");
		scn.MoveCompanionsToTable(warrior, warrior2);

		scn.StartGame();
		scn.SkipToSite(3);
		// Both markers are past sites 1 and 2.
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(scn.GetSite(1)));
		assertTrue(scn.ShadowControls(scn.GetSite(2)));
		scn.MoveCardsToHand(sword, armor, hall, pipe);
		assertInHand(sword, armor, hall, pipe);
		scn.FreepsRefreshDecision();

		// The Hall (a condition a [rohan] Man may bear) and the Pipe (a possession for Frodo) can each be played.
		assertTrue(scn.FreepsPlayAvailable(hall));
		assertTrue(scn.FreepsPlayAvailable(pipe));
		assertTrue(scn.FreepsActionAvailable(warrior));

		scn.FreepsUseCardAction(warrior);
		assertTrue(scn.FreepsHasCardChoiceAvailable(sword, armor));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(hall, pipe));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(sword);

		// The other Warrior could bear the Sword as well, and is not asked for.
		assertAttachedTo(sword, warrior);
		assertNotAttachedTo(sword, warrior2);
		assertInHand(armor, hall, pipe);
		// The highest site the opponent controls goes back on the path; the other stays his.
		assertFalse(scn.ShadowControls(scn.GetSite(2)));
		assertInZone(Zone.ADVENTURE_PATH, scn.GetSite(2));
		assertTrue(scn.ShadowControls(scn.GetSite(1)));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWithoutAPossessionHeCanBearAndUsedForNothingWithNoSiteToLiberate() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior = scn.GetFreepsCard("warrior");
		var sword = scn.GetFreepsCard("sword");
		var hall = scn.GetFreepsCard("hall");
		var pipe = scn.GetFreepsCard("pipe");
		scn.MoveCompanionsToTable(warrior);
		scn.MoveCardsToHand(hall, pipe);
		assertInHand(hall, pipe);

		scn.StartGame();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(hall));
		assertTrue(scn.FreepsPlayAvailable(pipe));
		assertFalse(scn.FreepsActionAvailable(warrior));

		scn.MoveCardsToHand(sword);
		scn.FreepsRefreshDecision();
		assertInHand(sword);
		assertTrue(scn.FreepsActionAvailable(warrior));

		// On the first turn nobody controls a site.  The Sword, the one possession he can bear, is played unasked.
		assertFalse(scn.ShadowControls(scn.GetSite(1)));
		assertFalse(scn.FreepsControls(scn.GetSite(1)));
		scn.FreepsUseCardAction(warrior);
		assertAttachedTo(sword, warrior);
		assertInHand(hall, pipe);
		assertFalse(scn.ShadowControls(scn.GetSite(1)));
		assertFalse(scn.FreepsControls(scn.GetSite(1)));
		assertInZone(Zone.ADVENTURE_PATH, scn.GetSite(1));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
