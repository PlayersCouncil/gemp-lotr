package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_114_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("damrod", "4_114");
					put("faramir", "4_117");      // Faramir, Son of Denethor: Ring-bound [gondor] Man, cost 3; bars only Shadow skirmish actions
					put("boromir", "0_65");       // Boromir, Steward's Heir: Ring-bound [gondor] Man; keywords only
					put("sam", "1_311");          // Sam, Son of Hamfast: a Ring-bound Hobbit who is not the Ring-bearer
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: an unbound Man, strength 8; a maneuver ability only

					put("regiment", "4_244");     // Regiment of Haradrim: [raider] Man, strength 16, vitality 4; a regroup ability only; cheated in past his spot
					put("wildmen", "4_4");        // Band of Wild Men: [dunland] Man, strength 11, vitality 2; a trigger on its own win only
					put("axeman", "4_224");       // Easterling Axeman: [raider] Man, strength 6, vitality 2; +3 and fierce only at 2 burdens
					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, no Man, vitality 3; damage +1 only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DamrodStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Damrod, Ranger of Ithilien
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Ring-bound</b>. <b>Ranger</b>. To play, spot a Ring-bound Man.<br><b>Skirmish:</b> Exert Damrod to exhaust a Man he is skirmishing.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("damrod");

		assertEquals("Damrod", card.getBlueprint().getTitle());
		assertEquals("Ranger of Ithilien", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RING_BOUND));
		assertTrue(scn.HasKeyword(card, Keyword.RANGER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void CannotBePlayedUntilARingBoundManIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var damrod = scn.GetFreepsCard("damrod");
		var faramir = scn.GetFreepsCard("faramir");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveCardsToHand(damrod, faramir);

		scn.StartGame();
		assertInHand(damrod);
		assertInHand(faramir);

		// Frodo and Sam are Ring-bound but no Men; Aragorn is a Man but unbound.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(damrod));

		scn.FreepsPlayCard(faramir);
		assertInZone(Zone.FREE_CHARACTERS, faramir);

		assertTrue(scn.FreepsPlayAvailable(damrod));
		scn.FreepsPlayCard(damrod);
		assertInZone(Zone.FREE_CHARACTERS, damrod);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ARingBoundManOfAnotherTitleLetsHimBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var damrod = scn.GetFreepsCard("damrod");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToHand(damrod);

		scn.StartGame();
		assertInHand(damrod);

		assertTrue(scn.FreepsPlayAvailable(damrod));
		scn.FreepsPlayCard(damrod);
		assertInZone(Zone.FREE_CHARACTERS, damrod);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityExertsDamrodToExhaustAManHeIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var damrod = scn.GetFreepsCard("damrod");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(damrod, aragorn);

		var regiment = scn.GetShadowCard("regiment");
		var wildmen = scn.GetShadowCard("wildmen");
		var axeman = scn.GetShadowCard("axeman");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(regiment, wildmen, axeman, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{damrod, regiment},
				new PhysicalCardImpl[]{aragorn, axeman});
		// The two left over go to Damrod as well: a second Man, of [dunland], and an Uruk-hai. Each can still exert,
		// since a character already exhausted is no candidate for exhausting.
		scn.ShadowAssignToMinions(damrod, wildmen, savage);

		scn.FreepsResolveSkirmish(damrod);
		assertEquals(0, scn.GetWoundsOn(damrod));
		assertEquals(0, scn.GetWoundsOn(regiment));
		assertTrue(scn.FreepsActionAvailable(damrod));
		scn.FreepsUseCardAction(damrod);
		assertEquals(1, scn.GetWoundsOn(damrod));

		// The Men he is skirmishing; not the Uruk-hai he is skirmishing, nor the Man waiting for Aragorn.
		assertTrue(scn.FreepsHasCardChoiceAvailable(regiment, wildmen));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(savage, axeman));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(regiment);

		// Vitality 4: exhausted is 3 wounds (exerting twice would be 2).
		assertEquals(3, scn.GetWoundsOn(regiment));
		assertEquals(0, scn.GetWoundsOn(wildmen));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(axeman));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void UsedInAnotherCompanionsSkirmishDamrodExertsAndNobodyIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var damrod = scn.GetFreepsCard("damrod");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(damrod, aragorn);

		var regiment = scn.GetShadowCard("regiment");
		var axeman = scn.GetShadowCard("axeman");

		scn.StartGame();
		scn.MoveMinionsToTable(regiment, axeman);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{damrod, regiment},
				new PhysicalCardImpl[]{aragorn, axeman});

		// Aragorn's skirmish first: Damrod's Man is assigned to him and not yet skirmishing, and Aragorn's Man is
		// skirmishing someone else.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, axeman));
		assertTrue(scn.IsCharAssignedAgainst(damrod, regiment));
		assertEquals(0, scn.GetWoundsOn(damrod));

		// The exertion can be paid; the Man is part of the effect.
		assertTrue(scn.FreepsActionAvailable(damrod));
		scn.FreepsUseCardAction(damrod);

		assertEquals(1, scn.GetWoundsOn(damrod));
		assertEquals(0, scn.GetWoundsOn(axeman));
		assertEquals(0, scn.GetWoundsOn(regiment));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithDamrodExhaustedItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var damrod = scn.GetFreepsCard("damrod");
		scn.MoveCompanionsToTable(damrod);
		// Vitality 3 with two wounds: exhausted.
		scn.AddWoundsToChar(damrod, 2);

		var regiment = scn.GetShadowCard("regiment");

		scn.StartGame();
		assertEquals(2, scn.GetWoundsOn(damrod));
		scn.MoveMinionsToTable(regiment);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(damrod, regiment);
		scn.FreepsResolveSkirmish(damrod);

		assertTrue(scn.IsCharSkirmishingAgainst(damrod, regiment));
		assertEquals(2, scn.GetWoundsOn(damrod));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(damrod));
	}
}
