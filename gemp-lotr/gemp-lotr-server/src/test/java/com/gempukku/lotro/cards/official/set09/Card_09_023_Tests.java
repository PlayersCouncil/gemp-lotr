package com.gempukku.lotro.cards.official.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_09_023_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("vilya", "9_23");
					// Elrond, Venerable Lord: [elven] Elf companion; his start-of-skirmish trigger needs him in a
					// skirmish, which he never is here
					put("elrondcomp", "10_9");
					// Elrond, Lord of Rivendell: [elven] Elf ally, home site 3; heals allies of site 3 at the start
					// of his owner's turns, and a fellowship ability used only on purpose
					put("elrondally", "1_40");
					// Gil-galad, Elven High King: [elven] Elf companion, strength 9, vitality 4; a regroup ability
					// used only on purpose
					put("gilgalad", "9_15");
					// Lórien Elf: Elf companion, strength 4, cost 1, no text but "To play, spot an Elf" (cheated in)
					put("elf", "1_53");
					// Legolas, Greenleaf: Elf companion; an archery ability used only on purpose
					put("legolas", "1_50");
					// Silinde, Elf of Mirkwood: Elf ally, home site 3, strength 5, vitality 2; has the text of site 3
					// (Rivendell Valley: a fellowship action) while it can be spotted
					put("silinde", "1_60");
					// Boromir, Son of Denethor: [gondor] Man companion, strength 7, vitality 3; a skirmish ability
					// used only on purpose
					put("boromir", "1_97");
					// Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, cost 2; Damage +1 is its whole text
					put("savage", "1_151");
					// Footman of Dunland: [men] Man, strength 7, vitality 3, cost 3; the site gains plains
					put("footman", "11_82");
					// Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, cost 6, no text
					put("troop", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void VilyaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Vilya, Ring of Air
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 0
		 * Type: Artifact
		 * Subtype: Ring
		 * Vitality: 1
		 * Game Text: Bearer must be Elrond or Gil-galad.
		 * 	<b>Skirmish:</b> Discard the top card of your draw deck. Make a minion skirmishing an Elf strength -X, where X is the twilight cost of the discarded card. Discard this artifact.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("vilya");

		assertEquals("Vilya", card.getBlueprint().getTitle());
		assertEquals("Ring of Air", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.RING));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void PlaysOnElrondOrGilgaladAndNoOtherElf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vilya = scn.GetFreepsCard("vilya");
		var elrond = scn.GetFreepsCard("elrondcomp");
		var gilgalad = scn.GetFreepsCard("gilgalad");
		var elf = scn.GetFreepsCard("elf");
		scn.MoveCompanionsToTable(elrond, gilgalad, elf);
		scn.MoveCardsToHand(vilya);

		scn.StartGame();

		assertInHand(vilya);
		assertTrue(scn.FreepsPlayAvailable(vilya));
		scn.FreepsPlayCard(vilya);

		assertTrue(scn.FreepsHasCardChoiceAvailable(elrond, gilgalad));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(elf));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gilgalad);

		assertAttachedTo(vilya, gilgalad);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlaysOnElrondTheAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vilya = scn.GetFreepsCard("vilya");
		var elrond = scn.GetFreepsCard("elrondally");
		var elf = scn.GetFreepsCard("elf");
		scn.MoveCompanionsToTable(elf);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToHand(vilya);

		scn.StartGame();

		assertInHand(vilya);
		assertTrue(scn.FreepsPlayAvailable(vilya));
		// Elrond is the one bearer the text allows: it goes on him unasked.
		scn.FreepsPlayCard(vilya);

		assertAttachedTo(vilya, elrond);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWithOnlyElvesOfOtherTitles() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vilya = scn.GetFreepsCard("vilya");
		var elf = scn.GetFreepsCard("elf");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(elf, legolas);
		scn.MoveCardsToHand(vilya);

		scn.StartGame();

		assertInHand(vilya);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(vilya));
	}

	@Test
	public void SkirmishAbilityDiscardsTheTopCardToMakeAMinionSkirmishingAnElfWeakerByItsCostAndIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vilya = scn.GetFreepsCard("vilya");
		var gilgalad = scn.GetFreepsCard("gilgalad");
		var elf = scn.GetFreepsCard("elf");
		var boromir = scn.GetFreepsCard("boromir");
		var topcard = scn.GetFreepsCard("footman");
		var shadowtop = scn.GetShadowCard("elf");
		var savage = scn.GetShadowCard("savage");
		var footman = scn.GetShadowCard("footman");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gilgalad, elf, boromir);
		scn.AttachCardsTo(gilgalad, vilya);

		scn.StartGame();
		// His own copy of Footman of Dunland (twilight cost 3) on top of his deck; another card on the Shadow deck.
		scn.MoveCardsToTopOfDeck(topcard, shadowtop);
		assertSame(topcard, scn.GetFreepsTopOfDeck());
		assertInZone(Zone.DECK, topcard);
		assertSame(shadowtop, scn.GetShadowTopOfDeck());
		assertAttachedTo(vilya, gilgalad);
		scn.MoveMinionsToTable(savage, footman, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{elf, savage},
				new PhysicalCardImpl[]{boromir, troop}
		);
		scn.ShadowAssignToMinions(elf, footman);
		scn.FreepsResolveSkirmish(elf);

		int deck = scn.GetFreepsDeckCount();
		int shadowDeck = scn.GetShadowDeckCount();
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(7, scn.GetStrength(footman));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(vilya));

		scn.FreepsUseCardAction(vilya);
		assertInDiscard(topcard);
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertSame(shadowtop, scn.GetShadowTopOfDeck());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());

		// Both minions in the Elf's skirmish; not the Troop, assigned to Boromir, whose skirmish is still to come.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, footman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(footman);

		// 7 - 3, the discarded card's twilight cost.
		assertEquals(4, scn.GetStrength(footman));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(13, scn.GetStrength(troop));
		assertInDiscard(vilya);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void UsedInTheSkirmishOfACompanionWhoIsNoElfItPaysAndIsDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vilya = scn.GetFreepsCard("vilya");
		var gilgalad = scn.GetFreepsCard("gilgalad");
		var elf = scn.GetFreepsCard("elf");
		var boromir = scn.GetFreepsCard("boromir");
		var topcard = scn.GetFreepsCard("footman");
		var savage = scn.GetShadowCard("savage");
		var footman = scn.GetShadowCard("footman");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gilgalad, elf, boromir);
		scn.AttachCardsTo(gilgalad, vilya);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(topcard);
		assertSame(topcard, scn.GetFreepsTopOfDeck());
		assertInZone(Zone.DECK, topcard);
		assertAttachedTo(vilya, gilgalad);
		scn.MoveMinionsToTable(savage, footman, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{elf, savage},
				new PhysicalCardImpl[]{boromir, troop}
		);
		scn.ShadowAssignToMinions(elf, footman);
		scn.FreepsResolveSkirmish(boromir);

		int deck = scn.GetFreepsDeckCount();
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(7, scn.GetStrength(footman));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(vilya));

		scn.FreepsUseCardAction(vilya);
		// The Troop fights a companion who is no Elf, and the two minions assigned to the Elf are not skirmishing
		// yet: the top card goes and so does Vilya, and no minion is made weaker.
		assertInDiscard(topcard, vilya);
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(7, scn.GetStrength(footman));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AMinionSkirmishingAnElfAllyIsMadeWeakerByACheaperCardForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vilya = scn.GetFreepsCard("vilya");
		var gilgalad = scn.GetFreepsCard("gilgalad");
		var silinde = scn.GetFreepsCard("silinde");
		var topcard = scn.GetFreepsCard("elf");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gilgalad);
		scn.MoveAlliesToTable(silinde);
		scn.AttachCardsTo(gilgalad, vilya);

		scn.StartGame();
		assertAttachedTo(vilya, gilgalad);
		scn.SkipToSite(2);
		// His own Lórien Elf (twilight cost 1) on top of his deck.
		scn.MoveCardsToTopOfDeck(topcard);
		assertSame(topcard, scn.GetFreepsTopOfDeck());
		assertInZone(Zone.DECK, topcard);
		scn.MoveMinionsToTable(savage, troop);
		// The fellowship moves to site 3, Silinde's home, where she can skirmish.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{silinde, savage},
				new PhysicalCardImpl[]{gilgalad, troop}
		);
		scn.FreepsResolveSkirmish(silinde);

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(vilya));

		scn.FreepsUseCardAction(vilya);
		// The Savage is the one minion skirmishing an Elf, and is chosen unasked.  Read at the next decision of the
		// same skirmish: 5 - 1.
		assertInDiscard(topcard, vilya);
		assertEquals(4, scn.GetStrength(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		// Silinde (5) beats the Savage (4) without overwhelming it; it takes a wound and lives.
		assertEquals(1, scn.GetWoundsOn(savage));
		scn.FreepsResolveSkirmish(gilgalad);

		// A skirmish ability lasts for the skirmish phase it is used in.
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vilya = scn.GetFreepsCard("vilya");
		var gilgalad = scn.GetFreepsCard("gilgalad");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gilgalad);
		scn.AttachCardsTo(gilgalad, vilya);

		scn.StartGame();
		assertAttachedTo(vilya, gilgalad);
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.GetFreepsDeckCount() > 0);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(vilya));
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWithAnEmptyDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vilya = scn.GetFreepsCard("vilya");
		var gilgalad = scn.GetFreepsCard("gilgalad");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gilgalad);
		scn.AttachCardsTo(gilgalad, vilya);

		scn.StartGame();
		scn.SetFreepsDrawDeck();
		assertEquals(0, scn.GetFreepsDeckCount());
		assertAttachedTo(vilya, gilgalad);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gilgalad, savage);
		scn.FreepsResolveSkirmish(gilgalad);

		assertEquals(0, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(vilya));
	}
}
