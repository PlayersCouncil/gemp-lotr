package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_278_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("chain", "4_278");
					put("chain2", "4_278");

					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion; a skirmish ability used only on purpose
					put("guard1", "5_83");      // Household Guard: [rohan] Man companion, strength 6, vitality 3, valiant only
					put("guard2", "5_83");
					put("barliman", "1_70");    // Barliman Butterbur: [gandalf] Man ally; a fellowship ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf companion; a skirmish ability used only on purpose
					put("erland", "2_21");      // Erland: [gandalf] Man ally, home site 3, strength 5, vitality 2; his text only bars looking at his owner's hand
					put("hugin", "2_24");       // Hugin: [gandalf] Man ally, home site 3, strength 4, vitality 2; a fellowship ability used only on purpose

					put("troop1", "1_177");     // Goblin Patrol Troop: strength 13, vitality 3, no text
					put("troop2", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HeavyChainStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Heavy Chain
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Armor
		 * Game Text: Bearer must be a Man.<br>Bearer may not be overwhelmed unless his or her strength is tripled.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("chain");

		assertEquals("Heavy Chain", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.ARMOR));
		assertEquals(1, card.getBlueprint().getPossessionClasses().size());
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnAManCompanionOrAManAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chain = scn.GetFreepsCard("chain");
		var boromir = scn.GetFreepsCard("boromir");
		var guard = scn.GetFreepsCard("guard1");
		var barliman = scn.GetFreepsCard("barliman");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(chain);
		scn.MoveCompanionsToTable(boromir, guard, gimli);
		scn.MoveAlliesToTable(barliman);

		scn.StartGame();

		assertInHand(chain);
		assertTrue(scn.FreepsPlayAvailable(chain));
		scn.FreepsPlayCard(chain);
		// Man companions of two cultures and a Man ally; not a Dwarf, nor a Hobbit.
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, guard, barliman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(barliman);

		assertAttachedTo(chain, barliman);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BearerIsNotOverwhelmedAtDoubleStrengthAndAnotherManIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chain = scn.GetFreepsCard("chain");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		scn.MoveCompanionsToTable(guard1, guard2);
		scn.AttachCardsTo(guard1, chain);

		scn.StartGame();
		assertAttachedTo(chain, guard1);
		scn.MoveMinionsToTable(troop1, troop2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard1, troop1}, new PhysicalCardImpl[]{guard2, troop2});

		// 13 against 6: more than double, less than triple. The Chain spares him; he takes the 1 wound for losing.
		scn.FreepsResolveSkirmish(guard1);
		assertAttachedTo(chain, guard1);
		assertEquals(6, scn.GetStrength(guard1));
		assertEquals(13, scn.GetStrength(troop1));
		assertEquals(0, scn.GetWoundsOn(guard1));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(guard1));
		assertInPlay(guard1);

		// The same against the other Guard, who bears no Chain: overwhelmed.
		scn.FreepsResolveSkirmish(guard2);
		assertEquals(6, scn.GetStrength(guard2));
		assertEquals(0, scn.GetFreepsDeadCount());
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, guard2);
		assertEquals(1, scn.GetFreepsDeadCount());

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ABearerWhoIsAnAllyIsNotOverwhelmedAtDoubleStrengthAndAnotherAllyIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chain = scn.GetFreepsCard("chain");
		var erland = scn.GetFreepsCard("erland");
		var hugin = scn.GetFreepsCard("hugin");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		scn.MoveAlliesToTable(erland, hugin);
		scn.AttachCardsTo(erland, chain);

		scn.StartGame();
		assertAttachedTo(chain, erland);
		scn.SkipToSite(2);
		scn.FreepsPass();
		// Site 3: the home of both allies, where they may be assigned.
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(troop1, troop2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{erland, troop1}, new PhysicalCardImpl[]{hugin, troop2});

		// 13 against 5: more than double, less than triple. The Chain spares the ally; he takes the 1 wound for losing.
		scn.FreepsResolveSkirmish(erland);
		assertAttachedTo(chain, erland);
		assertEquals(5, scn.GetStrength(erland));
		assertEquals(13, scn.GetStrength(troop1));
		assertEquals(0, scn.GetWoundsOn(erland));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(erland));
		assertInPlay(erland);

		// The same against the other ally, who bears no Chain: overwhelmed.
		scn.FreepsResolveSkirmish(hugin);
		assertEquals(4, scn.GetStrength(hugin));
		assertEquals(13, scn.GetStrength(troop2));
		assertEquals(0, scn.GetFreepsDeadCount());
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, hugin);
		assertEquals(1, scn.GetFreepsDeadCount());

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void BearerIsOverwhelmedAtExactlyTripleStrengthAndNotOneShortOfIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chain = scn.GetFreepsCard("chain");
		var chain2 = scn.GetFreepsCard("chain2");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		scn.MoveCompanionsToTable(guard1, guard2);
		scn.AttachCardsTo(guard1, chain);
		scn.AttachCardsTo(guard2, chain2);

		scn.StartGame();
		assertAttachedTo(chain, guard1);
		assertAttachedTo(chain2, guard2);
		scn.MoveMinionsToTable(troop1, troop2);
		// Strengths that only set the board: 13 + 4 = 17, one short of triple a Guard's 6; 13 + 5 = 18, exactly triple.
		scn.ApplyAdHocModifier(new StrengthModifier(null, troop1, null, 4));
		scn.ApplyAdHocModifier(new StrengthModifier(null, troop2, null, 5));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard1, troop1}, new PhysicalCardImpl[]{guard2, troop2});

		scn.FreepsResolveSkirmish(guard1);
		assertAttachedTo(chain, guard1);
		assertEquals(6, scn.GetStrength(guard1));
		assertEquals(17, scn.GetStrength(troop1));
		assertEquals(0, scn.GetWoundsOn(guard1));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(guard1));
		assertInPlay(guard1);

		scn.FreepsResolveSkirmish(guard2);
		assertAttachedTo(chain2, guard2);
		assertEquals(6, scn.GetStrength(guard2));
		assertEquals(18, scn.GetStrength(troop2));
		assertEquals(0, scn.GetWoundsOn(guard2));
		assertEquals(0, scn.GetFreepsDeadCount());
		scn.PassSkirmishActions();
		// Vitality 3 and unwounded: the one wound for losing would not kill him; the overwhelm does.
		assertInZone(Zone.DEAD, guard2);
		assertEquals(1, scn.GetFreepsDeadCount());

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
