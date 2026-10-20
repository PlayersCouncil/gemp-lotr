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

public class Card_09_009_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ring", "9_9");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRingScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ring", "9_9");
					put("gimli", "1_13");       // Gimli, Son of Glóin: Dwarf companion, strength 6, vitality 3; damage +1
					// Farin, Dwarven Emissary: Dwarf companion, strength 5 and +2 while skirmishing an Orc
					put("farin", "1_11");
					put("grimir", "1_17");      // Grimir, Dwarven Elder: a Dwarf ally; his fellowship ability is not used
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Dwarf
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					// Orc Chieftain: [sauron] Orc, strength 7, vitality 2; its text answers a weapon played on it
					put("chieftain", "1_266");
					// Moria Scout: [moria] Orc, vitality 2; its text answers its own play and an Elf spotted then
					put("scout", "1_191");
					// Troop of Uruk-hai: an Uruk-hai, no Orc; strength 9, vitality 4.  Its text is about weapons in its
					// own skirmish
					put("uruk", "1_143");
					// Half-troll of Far Harad: an [orc] minion who is no Orc; its start-of-assignment trigger wants
					// more companions than minions
					put("halftroll", "14_12");
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man, strength 8, vitality 1, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RingofRetributionStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Ring of Retribution
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Artifact
		 * Subtype: Ring
		 * Strength: 1
		 * Vitality: 1
		 * Game Text: Bearer must be a Dwarf.<br><b>Response:</b> If bearer wins a skirmish, discard this artifact to exert each Orc.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ring");

		assertEquals("Ring of Retribution", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.RING));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void PlaysOnADwarfCompanionOrAllyAndNotOnAnotherCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingScenario();

		var ring = scn.GetFreepsCard("ring");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gimli, farin, aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(ring);

		scn.StartGame();
		assertInHand(ring);
		assertTrue(scn.FreepsPlayAvailable(ring));

		scn.FreepsPlayCard(ring);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, farin, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(grimir);

		assertAttachedTo(ring, grimir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhenBearerWinsASkirmishTheRingIsDiscardedToExertEachOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingScenario();

		var ring = scn.GetFreepsCard("ring");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, ring);
		assertAttachedTo(ring, gimli);

		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var chieftain = scn.GetShadowCard("chieftain");
		var scout = scn.GetShadowCard("scout");
		var uruk = scn.GetShadowCard("uruk");
		var halftroll = scn.GetShadowCard("halftroll");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, troop, chieftain, scout, uruk, halftroll);
		// The Scout is exhausted: vitality 2, one wound.
		scn.AddWoundsToChar(scout, 1);
		assertEquals(1, scn.GetWoundsOn(scout));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, runner);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);

		assertAttachedTo(ring, gimli);
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertEquals(1, scn.GetWoundsOn(scout));
		assertEquals(0, scn.GetWoundsOn(uruk));
		assertEquals(0, scn.GetWoundsOn(halftroll));
		assertEquals(0, scn.GetWoundsOn(gimli));

		scn.PassCurrentPhaseActions();
		// Gimli, 6 + 1, beats the Runner's 5, who dies of the wound.
		// Engine pass 54: what answers the win comes before the loser loses.
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ring));
		scn.FreepsAcceptOptionalTrigger();

		assertInDiscard(ring);
		// Each Orc, of either culture and in no skirmish, exerts; the exhausted Orc cannot.
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(chieftain));
		assertEquals(1, scn.GetWoundsOn(scout));
		assertInZone(Zone.SHADOW_CHARACTERS, scout);
		// An Uruk-hai and an [orc] minion who is no Orc are no Orcs.
		assertEquals(0, scn.GetWoundsOn(uruk));
		assertEquals(0, scn.GetWoundsOn(halftroll));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenBearerLosesOrWhenAnotherDwarfWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingScenario();

		var ring = scn.GetFreepsCard("ring");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		scn.MoveCompanionsToTable(gimli, farin);
		scn.AttachCardsTo(gimli, ring);
		assertAttachedTo(ring, gimli);

		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, troop, chieftain);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, troop}, new PhysicalCardImpl[]{farin, runner});
		scn.ShadowDeclineAssignments();

		// Farin, a Dwarf who does not bear the Ring, 5 + 2 against an Orc, beats the Runner.
		scn.FreepsResolveSkirmish(farin);
		assertAttachedTo(ring, gimli);
		assertEquals(0, scn.GetWoundsOn(chieftain));
		scn.PassCurrentPhaseActions();
		assertInDiscard(runner);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(ring));

		// Gimli, 7 against 13, loses and lives: one wound, not overwhelmed.
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.FreepsResolveSkirmish(gimli);
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(ring));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		assertAttachedTo(ring, gimli);
		assertEquals(0, scn.GetWoundsOn(chieftain));
	}

	@Test
	public void WithNoOrcInPlayTheResponseIsStillOfferedAndDiscardsTheRingForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingScenario();

		var ring = scn.GetFreepsCard("ring");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, ring);
		assertAttachedTo(ring, gimli);

		var brigand = scn.GetShadowCard("brigand");
		var uruk = scn.GetShadowCard("uruk");

		scn.StartGame();
		scn.MoveMinionsToTable(brigand, uruk);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, brigand);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);

		// Gimli exerts for +2: 6 + 1 + 2 beats the Brigand's 8.
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.FreepsUseCardAction(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(uruk));
		scn.PassCurrentPhaseActions();

		// Engine pass 54: what answers the win comes before the loser loses.
		assertInZone(Zone.SHADOW_CHARACTERS, brigand);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ring));
		scn.FreepsAcceptOptionalTrigger();

		assertInDiscard(ring);
		assertEquals(0, scn.GetWoundsOn(uruk));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
