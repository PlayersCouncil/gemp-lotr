package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_193_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");           // Gimli, Son of Gloin: Dwarf companion, strength 6, vitality 3, damage +1; a skirmish ability used only on purpose
					put("thrarin", "1_27");         // Thrarin, Dwarven Smith: Dwarf ally, strength 4, vitality 2; a maneuver ability used only on purpose
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: a Man, strength 8, vitality 4; a maneuver ability used only on purpose
					// One Whom Men Would Follow: exert Aragorn; an ally is strength +2 and takes part in skirmishes until the regroup phase
					put("follow", "1_109");

					put("pursuer", "4_190");        // Uruk Pursuer: strength 5, vitality 3, fierce; its text is about unbound Hobbits
					put("column", "11_72");         // Column of Easterlings: strength 8, vitality 2; a skirmish ability used only on purpose
					put("savage", "1_151");         // Uruk Savage: strength 5, vitality 3, damage +1
					put("scrabbler", "2_64");       // Goblin Scrabbler: strength 4, vitality 1; its text is about its own play and roaming
				}},
				new HashMap<>()
				{{
					put("site1", "11_239");         // Fangorn Glade: its text answers a move to it; the game starts there
					put("starkhorn", "12_193");
					// Never laid in these tests.
					put("site3", "11_234");
					put("site5", "18_138");
					put("site6", "12_187");
					put("site7", "13_185");
					put("site8", "11_230");
					put("site9", "17_146");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void StarkhornStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Starkhorn
		 * Unique: false
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 3
		 * Direction: Right
		 * Game Text: <b>Mountain</b>. Each time your Dwarf wins a skirmish, you may draw a card.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite("starkhorn");

		assertEquals("Starkhorn", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.MOUNTAIN));
	}

	@Test
	public void EachSkirmishADwarfCompanionWinsOffersADrawWhichCanBeTakenOrDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		var pursuer = scn.GetShadowCard("pursuer");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("starkhorn"));

		var starkhorn = scn.GetCurrentSite();
		assertEquals("Starkhorn", starkhorn.getBlueprint().getTitle());
		scn.MoveMinionsToTable(pursuer);
		scn.ShadowRefreshDecision();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, pursuer);
		scn.FreepsResolveSkirmish(gimli);

		int hand = scn.GetFreepsHandCount();
		int deck = scn.GetFreepsDeckCount();
		int shadowHand = scn.GetShadowHandCount();
		assertTrue(deck > 0);
		scn.PassSkirmishActions();

		// Gimli's 6 beats the Pursuer's 5; the Pursuer takes 2 wounds (damage +1) and lives on, fierce.
		assertEquals(2, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(starkhorn));
		scn.FreepsAcceptOptionalTrigger();
		// The Dwarf's player draws, not the site's owner.
		assertEquals(hand + 1, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertEquals(shadowHand, scn.GetShadowHandCount());

		// The fierce round: Gimli wins a second skirmish in the same turn and is offered the draw again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(gimli, pursuer);
		scn.FreepsResolveSkirmish(gimli);
		scn.PassFierceSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(starkhorn));
		scn.FreepsDeclineOptionalTrigger();
		// Declined, it is not offered to the site's owner in his place.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(hand + 1, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertEquals(shadowHand, scn.GetShadowHandCount());
		assertInDiscard(pursuer);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ADwarfAllyWinningOffersTheDrawAndACompanionWhoIsNoDwarfDoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var thrarin = scn.GetFreepsCard("thrarin");
		var follow = scn.GetFreepsCard("follow");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToHand(follow);

		var savage = scn.GetShadowCard("savage");
		var scrabbler = scn.GetShadowCard("scrabbler");

		scn.StartGame(scn.GetFreepsSite("site1"));
		assertInHand(follow);
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("starkhorn"));

		var starkhorn = scn.GetCurrentSite();
		assertEquals("Starkhorn", starkhorn.getBlueprint().getTitle());
		scn.MoveMinionsToTable(savage, scrabbler);
		scn.ShadowRefreshDecision();

		// Thrarin is made to take part in skirmishes, at strength 6.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(follow);
		assertEquals(6, scn.GetStrength(thrarin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage}, new PhysicalCardImpl[]{thrarin, scrabbler});

		// Aragorn, 8, beats the Savage, 5: no Dwarf won.
		scn.FreepsResolveSkirmish(aragorn);
		int hand = scn.GetFreepsHandCount();
		int deck = scn.GetFreepsDeckCount();
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(starkhorn));
		assertEquals(hand, scn.GetFreepsHandCount());

		// Thrarin, 6, beats the Scrabbler, 4, and kills it.
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		scn.FreepsResolveSkirmish(thrarin);
		scn.PassSkirmishActions();
		assertInDiscard(scrabbler);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(starkhorn));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(hand + 1, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ADwarfLosingASkirmishAndLivingOffersNoDraw() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		var column = scn.GetShadowCard("column");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("starkhorn"));

		var starkhorn = scn.GetCurrentSite();
		assertEquals("Starkhorn", starkhorn.getBlueprint().getTitle());
		scn.MoveMinionsToTable(column);
		scn.ShadowRefreshDecision();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, column);
		scn.FreepsResolveSkirmish(gimli);

		int hand = scn.GetFreepsHandCount();
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.PassSkirmishActions();

		// The Column's 8 beats Gimli's 6, who takes a wound and lives.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(starkhorn));
		assertEquals(hand, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
