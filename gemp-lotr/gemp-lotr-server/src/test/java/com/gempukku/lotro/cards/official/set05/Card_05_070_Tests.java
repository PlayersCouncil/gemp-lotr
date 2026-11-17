package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_070_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("army", "5_70");

					put("mumak", "5_73");       // Mumak: [raider] mount of cost 2, bearer must be a Southron
					put("mumakil", "8_64");     // Mumakil: [raider] mount of cost 3, bearer must be a Southron
					put("warmumak", "5_78");    // War Mumak: [raider] mount, for the Free Peoples player's own discard pile
					put("spear", "4_255");      // Southron Spear: a [raider] possession of cost 1 that a Southron bears, no mount
					put("beast", "17_40");      // Beast of War: a [men] mount of cost 3 that a [men] Man bears

					put("nomad", "7_132");      // Desert Nomad: a second Southron; damage +1 only at 2 threats
					put("savage", "15_92");     // Savage Southron: a [men] Man (a bearer for Beast of War), no Southron; a maneuver ability only

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ArmyofHaradrimStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Army of Haradrim
		 * Unique: True
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 7
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 14
		 * Vitality: 4
		 * Site Number: 4
		 * Game Text: <b>Southron</b>.<br><b>Skirmish:</b> Remove (3) to play a [raider] mount from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("army");

		assertEquals("Army of Haradrim", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.SOUTHRON));
		assertEquals(7, card.getBlueprint().getTwilightCost());
		assertEquals(14, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void SkirmishAbilityRemoves3ToPlayARaiderMountFromYourDiscardPileOnASouthron() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var army = scn.GetShadowCard("army");
		var nomad = scn.GetShadowCard("nomad");
		var savage = scn.GetShadowCard("savage");
		var mumak = scn.GetShadowCard("mumak");
		var mumakil = scn.GetShadowCard("mumakil");
		var spear = scn.GetShadowCard("spear");
		var beast = scn.GetShadowCard("beast");
		scn.MoveCardsToDiscard(mumak, mumakil, spear, beast);
		// The Free Peoples player's own [raider] mount, in his own discard pile: a title in none of the Shadow player's piles.
		var warmumak = scn.GetFreepsCard("warmumak");
		scn.MoveCardsToDiscard(warmumak);
		assertInDiscard(mumak, mumakil, spear, beast, warmumak);

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(army, nomad, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, army);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(7);
		scn.FreepsPass();

		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(army));
		scn.ShadowUseCardAction(army);

		// [raider] mounts in his own discard pile; not a [raider] possession that is no mount, a mount of another
		// culture with a bearer on the table, or the other player's mount.
		assertTrue(scn.ShadowHasCardChoiceAvailable(mumak, mumakil));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(spear, beast, warmumak));
		scn.ShadowChooseCardBPFromSelection(mumakil);

		// The Mumakil goes on a Southron of the player's choice.
		assertTrue(scn.ShadowHasCardChoiceAvailable(army, nomad));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage, aragorn));
		scn.ShadowChooseCard(army);

		assertAttachedTo(mumakil, army);
		// 7 - 3 removed - 3 for the Mumakil
		assertEquals(1, scn.GetTwilight());
		assertInDiscard(mumak, spear, beast, warmumak);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithTooLittleTwilightForTheRemovalAndTheMountTheAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var army = scn.GetShadowCard("army");
		var mumak = scn.GetShadowCard("mumak");
		var spear = scn.GetShadowCard("spear");
		scn.MoveCardsToDiscard(mumak, spear);
		assertInDiscard(mumak, spear);

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(army);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, army);
		scn.FreepsResolveSkirmish(aragorn);
		// The Mumak costs 2: 3 + 2 = 5 is wanted, and 4 is one short.
		scn.SetTwilight(4);
		scn.FreepsPass();

		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(army));

		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(army));
		scn.ShadowUseCardAction(army);

		// The one mount there, on the one Southron: nothing asked.
		assertAttachedTo(mumak, army);
		// 5 - 3 removed - 2 for the Mumak
		assertEquals(0, scn.GetTwilight());
		assertInDiscard(spear);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoRaiderMountInYourDiscardPileTheAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var army = scn.GetShadowCard("army");
		var savage = scn.GetShadowCard("savage");
		var spear = scn.GetShadowCard("spear");
		var beast = scn.GetShadowCard("beast");
		scn.MoveCardsToDiscard(spear, beast);
		var warmumak = scn.GetFreepsCard("warmumak");
		scn.MoveCardsToDiscard(warmumak);
		assertInDiscard(spear, beast, warmumak);

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(army, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, army);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// A [raider] possession that is no mount, a [men] mount with a [men] Man to bear it, and the other player's
		// [raider] mount in his own pile: nothing the ability can play, so it is not offered.
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(army));
		assertInDiscard(spear, beast, warmumak);
	}
}
