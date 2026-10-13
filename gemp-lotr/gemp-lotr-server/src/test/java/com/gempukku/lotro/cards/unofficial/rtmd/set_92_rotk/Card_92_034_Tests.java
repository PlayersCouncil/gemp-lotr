package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertAttachedTo;
import static org.junit.Assert.*;

public class Card_92_034_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: [gondor] Man companion
					put("boromir", "1_96");      // Boromir, Lord of Gondor: [gondor] Man companion
					put("knight1", "5_35");      // Gondorian Knight: [gondor] Man companion, not unique, no text
					put("knight2", "5_35");
					put("legolas", "1_50");      // Legolas, Greenleaf: [elven] Elf companion; the Elf an Elven Cloak needs spotted
					put("merchant", "7_97");     // Gondorian Merchant: [gondor] Man who is an ally
					put("athelas1", "1_94");     // Athelas: possession of no class, cost 1; bearer must be a [gondor] Man
					put("athelas2", "1_94");
					put("sword", "1_112");       // Ranger's Sword: possession (hand weapon); bearer must be Aragorn
					put("cloak", "1_42");        // Elven Cloak: possession (cloak), cost 1; bearer must be a companion
					put("hobbitsword", "1_299"); // Hobbit Sword: possession (hand weapon); bearer must be a Hobbit
					put("narsil", "9_34");       // Narsil, Blade of the Faithful: artifact (hand weapon); bearer must be a [gondor] Man
					put("barahir", "6_55");      // Ring of Barahir: artifact (ring), cost 0; bearer must be Aragorn
					put("poison", "8_28");       // Spider Poison: Shadow possession of no class, here borne by a companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_34", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: [gondor] Man companion
					put("athelas1", "1_94");     // Athelas: possession of no class, cost 1; bearer must be a [gondor] Man
					put("athelas2", "1_94");
					put("ted", "18_75");         // Ted Sandyman, Chief's Men's Ally: [men] minion; his abilities are used only on purpose
					put("bow", "11_89");         // Long Battle Bow: Shadow possession (ranged weapon); bearer must be a [men] minion
					put("dagger", "18_69");      // Henchman's Dagger: Shadow possession (hand weapon), cost 0; bearer must be a [men] minion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_34"
		);
	}

	// The control: the same table with no modifier
	protected VirtualTableScenario GetNoModScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: [gondor] Man companion
					put("boromir", "1_96");      // Boromir, Lord of Gondor: [gondor] Man companion
					put("knight1", "5_35");      // Gondorian Knight: [gondor] Man companion, not unique, no text
					put("athelas1", "1_94");     // Athelas: possession of no class, cost 1; bearer must be a [gondor] Man
					put("athelas2", "1_94");
					put("sword", "1_112");       // Ranger's Sword: possession (hand weapon); bearer must be Aragorn
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, null
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_34
		 * Type: MetaSite
		 * Intensity: 3
		 * Game Text: Each companion may bear no more than one possession.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 92_34", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(3, card.getBlueprint().getIntensity());
	}

	@Test
	public void DifferentCompanionsCanEachBearOnePossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var sword = scn.GetFreepsCard("sword");
		var athelas1 = scn.GetFreepsCard("athelas1");
		var athelas2 = scn.GetFreepsCard("athelas2");

		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToHand(athelas1, athelas2, sword);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(sword));
		scn.FreepsPlayCard(sword);
		assertAttachedTo(sword, aragorn);

		assertTrue(scn.FreepsPlayAvailable(athelas1));
		scn.FreepsPlayCard(athelas1);
		assertAttachedTo(athelas1, boromir);

		//Can no longer play the second Athelas as both Aragorn and Boromir already have
		// the maximum number of attached possessions
		assertFalse(scn.FreepsPlayAvailable(athelas2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithoutTheModACompanionIsPlayedAndTransferredASecondAndAThirdPossession() throws DecisionResultInvalidException, CardNotFoundException {
		// The control: the same cards with no modifier on the table.
		var scn = GetNoModScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var knight1 = scn.GetFreepsCard("knight1");
		var sword = scn.GetFreepsCard("sword");
		var athelas1 = scn.GetFreepsCard("athelas1");
		var athelas2 = scn.GetFreepsCard("athelas2");

		scn.MoveCompanionsToTable(aragorn, boromir, knight1);
		scn.AttachCardsTo(aragorn, sword);
		scn.AttachCardsTo(boromir, athelas1);
		scn.MoveCardsToHand(athelas2);

		scn.StartGame();

		// Aragorn and Boromir each bear a possession already, and are offered beside the Knight who bears none
		assertTrue(scn.FreepsPlayAvailable(athelas2));
		scn.FreepsPlayCard(athelas2);
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, boromir, knight1));
		scn.FreepsChooseCard(aragorn);
		assertAttachedTo(athelas2, aragorn);
		assertAttachedTo(sword, aragorn);

		// Boromir's Athelas may be transferred to Aragorn as well as to the Knight
		assertTrue(scn.FreepsTransferAvailable(athelas1));
		scn.FreepsTransferCard(athelas1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, knight1));
		scn.FreepsChooseCard(aragorn);
		assertAttachedTo(athelas1, aragorn);
		assertAttachedTo(athelas2, aragorn);
		assertAttachedTo(sword, aragorn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OnlyACompanionBearingAPossessionIsBarredAndOnlyFromAnotherPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var knight1 = scn.GetFreepsCard("knight1");
		var knight2 = scn.GetFreepsCard("knight2");
		var legolas = scn.GetFreepsCard("legolas");
		var merchant = scn.GetFreepsCard("merchant");
		var sword = scn.GetFreepsCard("sword");
		var athelas1 = scn.GetFreepsCard("athelas1");
		var athelas2 = scn.GetFreepsCard("athelas2");
		var cloak = scn.GetFreepsCard("cloak");
		var hobbitsword = scn.GetFreepsCard("hobbitsword");
		var narsil = scn.GetFreepsCard("narsil");
		var barahir = scn.GetFreepsCard("barahir");
		var poison = scn.GetShadowCard("poison");

		scn.MoveCompanionsToTable(aragorn, boromir, knight1, knight2, legolas);
		scn.MoveAlliesToTable(merchant);
		// Three companions who each bear a possession: Aragorn's and Frodo's are their own weapons, and
		// Boromir's is the Shadow player's
		scn.AttachCardsTo(aragorn, sword);
		scn.AttachCardsTo(frodo, hobbitsword);
		scn.AttachCardsTo(boromir, poison);
		// A companion who bears an artifact, which is no possession; the second Knight and Legolas bear nothing
		scn.AttachCardsTo(knight1, narsil);
		// An ally who bears a possession, and is no companion
		scn.AttachCardsTo(merchant, athelas1);
		scn.MoveCardsToHand(athelas2, cloak, barahir);

		scn.StartGame();

		// Athelas goes on a [gondor] Man
		assertTrue(scn.FreepsPlayAvailable(athelas2));
		scn.FreepsPlayCard(athelas2);
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight1, knight2, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, boromir));
		scn.FreepsChooseCard(merchant);
		assertAttachedTo(athelas1, merchant);
		assertAttachedTo(athelas2, merchant);

		// An Elven Cloak goes on any companion
		assertTrue(scn.FreepsPlayAvailable(cloak));
		scn.FreepsPlayCard(cloak);
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight1, knight2, legolas));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, boromir, frodo));
		scn.FreepsChooseCard(knight1);
		assertAttachedTo(narsil, knight1);
		assertAttachedTo(cloak, knight1);

		// The Ring of Barahir is an artifact: Aragorn may bear it beside his sword
		assertTrue(scn.FreepsPlayAvailable(barahir));
		scn.FreepsPlayCard(barahir);
		assertAttachedTo(barahir, aragorn);
		assertAttachedTo(sword, aragorn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void APossessionIsNotTransferredToACompanionWhoBearsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var knight1 = scn.GetFreepsCard("knight1");
		var knight2 = scn.GetFreepsCard("knight2");
		var sword = scn.GetFreepsCard("sword");
		var athelas1 = scn.GetFreepsCard("athelas1");
		var narsil = scn.GetFreepsCard("narsil");

		scn.MoveCompanionsToTable(aragorn, boromir, knight1, knight2);
		scn.AttachCardsTo(aragorn, sword);
		scn.AttachCardsTo(boromir, athelas1);
		scn.AttachCardsTo(knight1, narsil);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsTransferAvailable(athelas1));
		scn.FreepsTransferCard(athelas1);
		// The Knight with an artifact and the Knight with nothing may take Boromir's Athelas; Aragorn, with his
		// sword, may not
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight1, knight2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		scn.FreepsChooseCard(knight2);

		assertAttachedTo(athelas1, knight2);
		// A transfer is paid for with the card's twilight cost
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AMinionBearingAPossessionMayBeGivenAnother() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var ted = scn.GetShadowCard("ted");
		var bow = scn.GetShadowCard("bow");
		var dagger = scn.GetShadowCard("dagger");

		scn.StartGame();
		scn.MoveMinionsToTable(ted);
		scn.AttachCardsTo(ted, bow);
		scn.MoveCardsToHand(dagger);
		scn.FreepsPass();

		// A ranged weapon and a hand weapon: only the modifier could stand in the way, and Ted is no companion
		assertTrue(scn.ShadowPlayAvailable(dagger));
		scn.ShadowPlayCard(dagger);
		assertAttachedTo(dagger, ted);
		assertAttachedTo(bow, ted);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AffectsOpponent() throws DecisionResultInvalidException, CardNotFoundException {
		// The text has no "your": the companions of the player across the table from the modifier are held to it too.
		var scn = GetShadowScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var athelas1 = scn.GetFreepsCard("athelas1");
		var athelas2 = scn.GetFreepsCard("athelas2");

		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, athelas1);
		scn.MoveCardsToHand(athelas2);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(athelas2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
