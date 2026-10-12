package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_013_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sharp", "12_13");
					// Gimli, Lively Combatant: Dwarf companion, strength 6, vitality 3, resistance 7, damage +1;
					// he may draw a card each time he wins a skirmish
					put("gimli", "11_8");
					// Dwarven Warrior: Dwarf companion, strength 6, vitality 3, resistance 6; his text is about
					// his being played
					put("warrior", "12_7");
					// Grimir, Dwarven Elder: a Dwarf who is an ally, strength 3, vitality 3
					put("grimir", "1_17");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Dwarf

					// Possessions a Dwarf bears.  Book of Mazarbul: no class; its text needs site 4.
					put("book", "3_1");
					put("helm", "1_15");        // Gimli's Helm: helm
					// Axe of Khazad-dum: hand weapon, strength +2; damage +1 only at a mountain
					put("axe", "11_3");
					put("armor", "1_8");        // Dwarven Armor: armor
					// Dwarven Bracers: bracers, strength +1; a Response when bearer is about to take a wound
					put("bracers", "12_5");
					// Spider Poison: a [gollum] possession, the Shadow player's, borne by a companion.  "Each
					// time bearer is assigned to a skirmish, bearer must exert."
					put("poison", "8_28");
					// Cards a Dwarf bears that are no possessions.  Ring of Guile: artifact, strength +1,
					// damage +1.  Dwarven Heart: condition; it exerts bearer at the start of his turns.
					put("guile", "9_8");
					put("heart", "1_10");

					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3, damage +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SharpDefenseStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Sharp Defense
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make a Dwarf strength +2 (or +2 for each possession he bears if he has resistance 4 or more).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sharp");

		assertEquals("Sharp Defense", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SharpDefenseMakesDwarfWith3ResistanceAndNoPossessionsStrengthPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var sharp = scn.GetFreepsCard("sharp");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToHand(sharp);
		scn.MoveCompanionsToTable(gimli);

		var savage = scn.GetShadowCard("savage");
		scn.MoveMinionsToTable(savage);

		scn.StartGame();
		// Gimli's resistance of 7, less 4 burdens
		scn.AddBurdens(4);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, savage);
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(3, scn.GetResistance(gimli));
		assertEquals(6, scn.GetStrength(gimli));
		assertInZone(Zone.HAND, sharp);

		scn.FreepsPlayCard(sharp);
		//Base 6, and since the 4 resistance clause is not met, +2 flat strength
		assertEquals(8, scn.GetStrength(gimli));
		assertInZone(Zone.DISCARD, sharp);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SharpDefenseMakesDwarfWith4ResistanceAndNoPossessionsStrengthPlus0() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var sharp = scn.GetFreepsCard("sharp");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToHand(sharp);
		scn.MoveCompanionsToTable(gimli);

		var savage = scn.GetShadowCard("savage");
		scn.MoveMinionsToTable(savage);

		scn.StartGame();
		// Gimli's resistance of 7, less 3 burdens
		scn.AddBurdens(3);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, savage);
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(4, scn.GetResistance(gimli));
		assertEquals(6, scn.GetStrength(gimli));
		assertInZone(Zone.HAND, sharp);

		scn.FreepsPlayCard(sharp);
		//Base 6, and since the 4 resistance clause is met, +0 for all attached possessions
		assertEquals(6, scn.GetStrength(gimli));
		// The event is spent all the same, and its player is given no choice of the +2.
		assertInZone(Zone.DISCARD, sharp);
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SharpDefenseMakesDwarfWith4ResistanceAndNoPossessionsStrengthPlus2ForEachPossessionBorne() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var sharp = scn.GetFreepsCard("sharp");
		var gimli = scn.GetFreepsCard("gimli");
		var book = scn.GetFreepsCard("book");
		var helm = scn.GetFreepsCard("helm");
		scn.MoveCardsToHand(sharp);
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, book, helm);

		var savage = scn.GetShadowCard("savage");
		scn.MoveMinionsToTable(savage);

		scn.StartGame();
		// Gimli's resistance of 7, less 3 burdens
		scn.AddBurdens(3);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, savage);
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(4, scn.GetResistance(gimli));
		assertEquals(6, scn.GetStrength(gimli));

		scn.FreepsPlayCard(sharp);
		//Base 6 + 2 for book + 2 for helm
		assertEquals(10, scn.GetStrength(gimli));
		assertInZone(Zone.DISCARD, sharp);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void EachPossessionOfEitherSideTheChosenDwarfBearsCountsAndNothingElseDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sharp = scn.GetFreepsCard("sharp");
		var gimli = scn.GetFreepsCard("gimli");
		var warrior = scn.GetFreepsCard("warrior");
		var axe = scn.GetFreepsCard("axe");
		var bracers = scn.GetFreepsCard("bracers");
		var armor = scn.GetFreepsCard("armor");
		var guile = scn.GetFreepsCard("guile");
		var heart = scn.GetFreepsCard("heart");
		var poison = scn.GetShadowCard("poison");
		scn.MoveCardsToHand(sharp);
		scn.MoveCompanionsToTable(gimli, warrior);
		// Gimli bears three possessions (a hand weapon, bracers, and the Shadow player's Spider Poison),
		// an artifact and a condition.  The Dwarf beside him bears a possession of his own.
		scn.AttachCardsTo(gimli, axe, bracers, guile, poison);
		scn.AttachCardsTo(warrior, armor);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.AttachCardsTo(gimli, heart);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, savage);
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(7, scn.GetResistance(gimli));
		assertAttachedTo(axe, gimli);
		assertAttachedTo(bracers, gimli);
		assertAttachedTo(poison, gimli);
		assertAttachedTo(guile, gimli);
		assertAttachedTo(heart, gimli);
		assertAttachedTo(armor, warrior);
		// 6 + 2 for the Axe + 1 for the Bracers + 1 for the Ring
		assertEquals(10, scn.GetStrength(gimli));
		assertEquals(6, scn.GetStrength(warrior));

		scn.FreepsPlayCard(sharp);
		scn.FreepsChooseCard(gimli);

		// +2 for each of the Axe, the Bracers and the Poison
		assertEquals(16, scn.GetStrength(gimli));
		assertEquals(6, scn.GetStrength(warrior));
		assertInZone(Zone.DISCARD, sharp);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AnyDwarfMayBeChosenAndADwarfAllyBearingPossessionsIsStrengthPlus2UntilTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sharp = scn.GetFreepsCard("sharp");
		var gimli = scn.GetFreepsCard("gimli");
		var warrior = scn.GetFreepsCard("warrior");
		var aragorn = scn.GetFreepsCard("aragorn");
		var grimir = scn.GetFreepsCard("grimir");
		var axe = scn.GetFreepsCard("axe");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCardsToHand(sharp);
		scn.MoveCompanionsToTable(gimli, warrior, aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.AttachCardsTo(grimir, axe, armor);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);

		// A Skirmish event: not to be played in the assignment phase.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertInZone(Zone.HAND, sharp);
		assertFalse(scn.FreepsPlayAvailable(sharp));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(warrior, savage);
		scn.FreepsResolveSkirmish(warrior);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(sharp));
		// 3 + 2 for the Axe
		assertEquals(5, scn.GetStrength(grimir));

		scn.FreepsPlayCard(sharp);
		// The Dwarf in the skirmish, a Dwarf companion in none, and a Dwarf who is an ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(warrior, gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, savage));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(grimir);

		// An ally has a resistance of zero: +2, where a Dwarf of resistance 4 with his two possessions
		// would be +4.
		assertEquals(7, scn.GetStrength(grimir));
		assertEquals(6, scn.GetStrength(warrior));
		assertEquals(6, scn.GetStrength(gimli));
		assertInZone(Zone.DISCARD, sharp);

		scn.PassCurrentPhaseActions();
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(5, scn.GetStrength(grimir));
	}
}
