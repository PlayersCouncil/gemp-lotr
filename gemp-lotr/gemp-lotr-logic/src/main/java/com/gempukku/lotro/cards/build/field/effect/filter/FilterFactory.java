package com.gempukku.lotro.cards.build.field.effect.filter;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.ValueResolver;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.logic.PlayUtils;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.effects.DiscardCardsFromPlayEffect;
import com.gempukku.lotro.logic.modifiers.evaluator.ConstantEvaluator;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;
import com.gempukku.lotro.logic.modifiers.evaluator.SingleMemoryEvaluator;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.results.CharacterLostSkirmishResult;

import java.util.*;

public class FilterFactory {
    private final Map<String, FilterableSource> simpleFilters = new HashMap<>();
    private final Map<String, FilterableSourceProducer> parameterFilters = new HashMap<>();

    public FilterFactory() {
        for (CardType value : CardType.values())
            appendFilter(value);
        for (Keyword value : Keyword.values())
            appendFilter(value);

        // A "support card": in a support area, which a card borne by a card in one is too (Q-0099).  The zone alone
        // is zone(support).
        appendFilter("support", "support", Filters.inSupportArea);

        for (PossessionClass value : PossessionClass.values())
            appendFilter(value);
        for (Race value : Race.values())
            appendFilter(value);
        for (Culture value : Culture.values()) {
            // Since there is already a Race of Orc and Uruk-hai, we can't add the Orc/Uruk-hai cultures as standalone filters
            if(value == Culture.ORC || value == Culture.URUK_HAI)
                continue;

            // The HDG runs into this same issue with the Spider and Troll cultures
            if(value == Culture.SPIDER || value == Culture.TROLL)
                continue;

            // Wraith is a race, but the original Ringwraith is not, so we'll support both "culture(wraith)" and "Ringwraith"
            if(value == Culture.WRAITH) {
                appendFilter("ringwraith", "ringwraith", value);
            }
            else {
                appendFilter(value);
            }
        }

        simpleFilters.put("another", (actionContext) -> Filters.not(actionContext.getSource()));
        simpleFilters.put("other", (actionContext) -> Filters.not(actionContext.getSource()));
        simpleFilters.put("any", (actionContext) -> Filters.any);
        simpleFilters.put("attachedtoinsameregion",
                actionContext -> {
                    final PhysicalCard attachedTo = actionContext.getSource().getAttachedTo();
                    return Filters.region(GameUtils.getRegion(attachedTo.getSiteNumber()));
                });
        simpleFilters.put("barredfromarcheryfire", (actionContext) -> Filters.barredFromArcheryFire);
        simpleFilters.put("bearer", (actionContext -> Filters.bearerOf(actionContext.getSource())));
        simpleFilters.put("canbediscarded", (actionContext -> Filters.canBeDiscarded(actionContext.getPerformingPlayer(), actionContext.getSource())));
        simpleFilters.put("canbereturnedtohand", (actionContext -> Filters.canBeReturnedToHand(actionContext.getSource())));
        simpleFilters.put("canexert", (actionContext -> Filters.canExert(actionContext.getSource())));
        simpleFilters.put("canwound", (actionContext -> Filters.canTakeWounds(actionContext.getSource(), 1)));
        simpleFilters.put("character", (actionContext) -> Filters.character);
        simpleFilters.put("controlledbyotherplayer",
                (actionContext -> Filters.siteControlledByOtherPlayer(actionContext.getPerformingPlayer())));
        simpleFilters.put("controlledbyshadowplayer",
                (actionContext -> Filters.siteControlledByOtherPlayer(actionContext.getGame().getGameState().getCurrentPlayerId())));
        simpleFilters.put("controlledsite",
                (actionContext -> Filters.siteControlledByPlayer(actionContext.getPerformingPlayer())));
        simpleFilters.put("siteyoucontrol",
                (actionContext -> Filters.siteControlledByPlayer(actionContext.getPerformingPlayer())));
        simpleFilters.put("cultureindeadpile",
                new FilterableSource() {
                    @Override
                    public Filterable getFilterable(ActionContext actionContext) {
                        LotroGame game = actionContext.getGame();
                        List<? extends PhysicalCard> deadPile = game.getGameState().getDeadPile(game.getGameState().getCurrentPlayerId());
                        return new Filter() {
                            @Override
                            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                                Culture culture = physicalCard.getBlueprint().getCulture();
                                return Filters.acceptsAny(game, deadPile, culture);
                            }
                        };
                    }
                }
        );
        simpleFilters.put("culturewithtokens",
                (actionContext -> {
                    final Set<Culture> cultureTokens = new HashSet<>();
                    LotroGame game = actionContext.getGame();
                    for (PhysicalCard physicalCard : Filters.filterActive(game, Filters.hasAnyCultureTokens(1))) {
                        Map<Token, Integer> tokens = game.getGameState().getTokens(physicalCard);
                        for (Map.Entry<Token, Integer> tokenIntegerEntry : tokens.entrySet()) {
                            if (tokenIntegerEntry.getValue() > 0) {
                                Culture culture = tokenIntegerEntry.getKey().getCulture();
                                if (culture != null)
                                    cultureTokens.add(culture);
                            }
                        }
                    }

                    return (Filter) (game1, physicalCard) -> {
                        Culture culture = physicalCard.getBlueprint().getCulture();
                        return cultureTokens.contains(culture);
                    };
                }));
        simpleFilters.put("currentsite",
                (actionContext) -> Filters.currentSite);
        simpleFilters.put("currentsitenumber",
                (actionContext -> Filters.siteNumber(actionContext.getGame().getGameState().getCurrentSiteNumber())));
        simpleFilters.put("nextsite",
                (actionContext) -> Filters.nextSite);
        simpleFilters.put("nextsitenumber",
                (actionContext -> Filters.siteNumber(actionContext.getGame().getGameState().getCurrentSiteNumber() + 1)));
        simpleFilters.put("exhausted", (actionContext) -> Filters.exhausted);
        simpleFilters.put("gettingdiscarded",
                (actionContext -> {
                    Effect effect = actionContext.getEffect();
                    if (effect instanceof DiscardCardsFromPlayEffect aboutToDiscardFromPlay) {
                        Collection<PhysicalCard> discardedCards = aboutToDiscardFromPlay.getAffectedCardsMinusPrevented(actionContext.getGame());
                        return Filters.in(discardedCards);
                    }
                    return Filters.none;
                }));
        simpleFilters.put("hasrace",
                (actionContext -> (Filter) (game, physicalCard) -> !game.getModifiersQuerying().getRaces(game, physicalCard).isEmpty()));
        // Must agree with what a Hinder effect from this card could actually hinder, i.e. it also honours
        // CantBeHindered modifiers (Endless Night counting itself as hinderable, #1014).
        simpleFilters.put("hinderable",
                (actionContext -> Filters.canBeHindered(actionContext.getSource())));
        simpleFilters.put("hindered",
                (actionContext -> (Filter) (game, physicalCard) -> game.getGameState().isHindered(physicalCard)));
        simpleFilters.put("idinstored",
                (actionContext ->
                        (Filter) (game, physicalCard) -> {
                            final PhysicalCard.WhileInZoneData whileInZoneData = actionContext.getSource().getWhileInZoneData();
                            if (whileInZoneData == null)
                                return false;
                            for (String cardId : whileInZoneData.getValue().split(",")) {
                                if (cardId.equals(String.valueOf(physicalCard.getCardId())))
                                    return true;
                            }

                            return false;
                        }));
        simpleFilters.put("infierceskirmish", (actionContext) -> Filters.inFierceSkirmish);
        simpleFilters.put("inplay", (actionContext) -> Filters.inPlay);
        simpleFilters.put("insameregion",
                actionContext -> Filters.region(GameUtils.getRegion(actionContext.getSource().getSiteNumber())));
        simpleFilters.put("inskirmish", (actionContext) -> Filters.inSkirmish);
        simpleFilters.put("item", (actionContext) -> Filters.item);
        simpleFilters.put("mounted", (actionContext) -> Filters.mounted);
        simpleFilters.put("notassignedtoskirmish",
                (actionContext) -> Filters.notAssignedToSkirmish);
        simpleFilters.put("oneperbearer", (actionContext) ->
                Filters.not(Filters.hasAttached(Filters.name(actionContext.getSource().getBlueprint().getTitle()))));
        simpleFilters.put("limitoneperbearer", simpleFilters.get("oneperbearer"));
        simpleFilters.put("limit1perbearer", simpleFilters.get("oneperbearer"));
        simpleFilters.put("oddtwilight", (actionContext) -> Filters.oddPrintedTwilightCost());
        simpleFilters.put("eventwilight", (actionContext) -> Filters.evenPrintedTwilightCost());
        simpleFilters.put("playable", (actionContext) -> Filters.playable(0));
        simpleFilters.put("playablefromdeadpile", (actionContext) -> Filters.playable(0, 0, actionContext.getUnpaidThreatCost(), false, true, false));
        simpleFilters.put("ringbearer", (actionContext) -> Filters.ringBearer);
        simpleFilters.put("ring-bearer", (actionContext) -> Filters.ringBearer);
        simpleFilters.put("ringbound",
                (actionContext) -> Filters.ringBoundCompanion);
        simpleFilters.put("ring-bound",
                (actionContext) -> Filters.ringBoundCompanion);
        simpleFilters.put("self", ActionContext::getSource);
        simpleFilters.put("sitehassitenumber",
                (actionContext) -> Filters.siteHasSiteNumber);
        simpleFilters.put("siteincurrentregion",
                (actionContext) -> Filters.siteInCurrentRegion);
        simpleFilters.put("skirmishloser",
                (actionContext) -> {
                    final CharacterLostSkirmishResult lostSkirmish = (CharacterLostSkirmishResult) actionContext.getEffectResult();
                    return lostSkirmish.getLoser();
                });
        simpleFilters.put("storedculture",
                (actionContext) -> (Filter) (game, card) -> {
                    PhysicalCard.WhileInZoneData data = actionContext.getSource().getWhileInZoneData();
                    if (data != null) {
                        return game.getModifiersQuerying().isCulture(game, card, Culture.findCultureByHumanReadable(data.getHumanReadable()));
                    }
                    return false;
                });
        simpleFilters.put("storedkeyword",
                (actionContext) -> (Filter) (game, physicalCard) -> {
                    PhysicalCard.WhileInZoneData data = actionContext.getSource().getWhileInZoneData();
                    if (data != null) {
                        Keyword keyword = Keyword.valueOf(data.getValue());
                        if (keyword != null) {
                            return game.getModifiersQuerying().hasKeyword(game, physicalCard, keyword);
                        }
                    }
                    return false;
                });
        simpleFilters.put("storedtitle",
                (actionContext) -> (Filter) (game, physicalCard) -> {
                    PhysicalCard.WhileInZoneData data = actionContext.getSource().getWhileInZoneData();
                    if (data != null) {
                        return physicalCard.getBlueprint().getTitle().equals(data.getValue());
                    }
                    return false;
                });
        simpleFilters.put("unbound",
                (actionContext) -> Filters.unboundCompanion);
        simpleFilters.put("uncontrolled",
                (actionContext) -> Filters.uncontrolledSite);
        simpleFilters.put("unique", (actionContext) -> Filters.unique);
        simpleFilters.put("unwounded", (actionContext) -> Filters.unwounded);
        simpleFilters.put("weapon", (actionContext) -> Filters.weapon);
        simpleFilters.put("wounded", (actionContext) -> Filters.wounded);
        simpleFilters.put("your", (actionContext) -> Filters.owner(actionContext.getPerformingPlayer()));

        parameterFilters.put("and",
                (parameter, environment) -> {
                    final String[] filters = splitIntoFilters(parameter);
                    FilterableSource[] filterables = new FilterableSource[filters.length];
                    for (int i = 0; i < filters.length; i++)
                        filterables[i] = environment.getFilterFactory().generateFilter(filters[i], environment);
                    return (actionContext) -> {
                        Filterable[] filters1 = new Filterable[filterables.length];
                        for (int i = 0; i < filterables.length; i++)
                            filters1[i] = filterables[i].getFilterable(actionContext);

                        return Filters.and(filters1);
                    };
                });
        parameterFilters.put("not",
                (parameter, environment) -> {
                    final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(parameter, environment);
                    return (actionContext) -> Filters.not(filterableSource.getFilterable(actionContext));
                });
        parameterFilters.put("or",
                (parameter, environment) -> {
                    final String[] filters = splitIntoFilters(parameter);
                    FilterableSource[] filterables = new FilterableSource[filters.length];
                    for (int i = 0; i < filters.length; i++)
                        filterables[i] = environment.getFilterFactory().generateFilter(filters[i], environment);
                    return (actionContext) -> {
                        Filterable[] filters1 = new Filterable[filterables.length];
                        for (int i = 0; i < filterables.length; i++)
                            filters1[i] = filterables[i].getFilterable(actionContext);

                        return Filters.or(filters1);
                    };
                });
        parameterFilters.put("allyhome",
                (parameter, environment) -> {
                    final String[] parameterSplit = parameter.split(",");
                    final SitesBlock sitesBlock = SitesBlock.findBlock(parameterSplit[0]);
                    int number = Integer.parseInt(parameterSplit[1]);
                    return (actionContext) -> Filters.isAllyHome(number, sitesBlock);
                });
        // A card at a site the filter given accepts, "while bearer is at a plains site": an ally at his home site,
        // every other card at the fellowship's (Filters.atSite; CR 4.2 "ally", F-0518).  Where the text names the
        // character's place; "while the fellowship is at" is the location requirement.
        parameterFilters.put("atsite",
                (parameter, environment) -> {
                    final FilterableSource siteFilterableSource = environment.getFilterFactory().generateFilter(parameter, environment);
                    return (actionContext) -> Filters.atSite(siteFilterableSource.getFilterable(actionContext));
                });
        parameterFilters.put("samenumberinyouradventuredeck",
                (parameter, environment) -> {
                    final FilterableSource deckSiteFilterableSource = environment.getFilterFactory().generateFilter(parameter, environment);
                    return (actionContext) -> Filters.sameNumberInAdventureDeck(actionContext.getPerformingPlayer(), deckSiteFilterableSource.getFilterable(actionContext));
                });
        parameterFilters.put("allyinregion",
                (parameter, environment) -> {
                    final String[] parameterSplit = parameter.split(",");
                    final SitesBlock sitesBlock = Enum.valueOf(SitesBlock.class, parameterSplit[0].toUpperCase().replace('_', ' '));
                    int number = Integer.parseInt(parameterSplit[1]);
                    return (actionContext) -> Filters.isAllyInRegion(number, sitesBlock);
                });

        // Whether a card's own text could assign the two to each other: what AssignFpCharacterToSkirmish goes on
        // to ask, with the flags that effect has when its definition names none.  An assignment already made is
        // not ignored and defender is, whichever player asks: an assignment action assigns only if "both of
        // them are unassigned" (CR 4.2 "assignment phase"), and defender +X is for the Free Peoples player's own
        // assigning, which no definition asks about (F-0355).
        parameterFilters.put("assignabletoskirmishagainst",
                (parameter, environment) -> {
                    final FilterableSource againstFilterableSource = environment.getFilterFactory().generateFilter(parameter, environment);
                    return actionContext -> {
                        final Side side = GameUtils.getSide(actionContext.getGame(), actionContext.getPerformingPlayer());
                        return Filters.assignableToSkirmishAgainst(side, againstFilterableSource.getFilterable(actionContext), false, true, false);
                    };
                });
        // As "assignableToSkirmishAgainst", asked of an ally as if a card had already made him participate in
        // skirmishes: where his home is does not count, and everything else that stops an assignment does (a
        // card that bars him from skirmishes, an assignment he or the other character already has, a card that
        // says either cannot be assigned).  It is what AssignFpCharacterToSkirmish goes on to ask once an
        // EnableParticipationInSkirmishes before it has been carried out, so a requirement and a choice written
        // with it withhold "make that ally participate in skirmishes and assign this minion to skirmish that
        // ally" when it could assign the minion to nobody, and offer only the allies it could (F-0283).
        parameterFilters.put("assignabletoskirmishagainstignoringallylocation",
                (parameter, environment) -> {
                    final FilterableSource againstFilterableSource = environment.getFilterFactory().generateFilter(parameter, environment);
                    return actionContext -> {
                        final Side side = GameUtils.getSide(actionContext.getGame(), actionContext.getPerformingPlayer());
                        return Filters.assignableToSkirmishAgainst(side, againstFilterableSource.getFilterable(actionContext), false, true, true);
                    };
                });
        parameterFilters.put("assignedtoskirmish",
                (parameter, environment) -> {
                    final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(parameter, environment);
                    return (actionContext) -> Filters.assignedToSkirmishAgainst(filterableSource.getFilterable(actionContext));
                });
        parameterFilters.put("attachedto",
                (parameter, environment) -> {
                    final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(parameter, environment);
                    return (actionContext) -> Filters.attachedTo(filterableSource.getFilterable(actionContext));
                });
        parameterFilters.put("stackedon",
                (parameter, environment) -> {
                    final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(parameter, environment);
                    return (actionContext) -> Filters.stackedOn(filterableSource.getFilterable(actionContext));
                });
        parameterFilters.put("cardtypefrommemory", ((parameter, environment) -> actionContext -> {
            Set<CardType> cardtypes = new HashSet<>();
            for (PhysicalCard physicalCard : actionContext.getCardsFromMemory(parameter)) {
                cardtypes.add(physicalCard.getBlueprint().getCardType());
            }
            return Filters.or(cardtypes.toArray(new CardType[0]));
        }));
        parameterFilters.put("culture", (parameter, environment) -> {
            final Culture culture = Culture.findCulture(parameter);
            if (culture == null)
                throw new InvalidCardDefinitionException("Unable to find culture for: " + parameter);

            return (actionContext) -> culture;
        });
        parameterFilters.put("culturefrommemory", ((parameter, environment) -> actionContext -> {
            Set<Culture> cultures = new HashSet<>();
            var game = actionContext.getGame();
            for (PhysicalCard card : actionContext.getCardsFromMemory(parameter)) {
                cultures.addAll(game.getModifiersQuerying().getCultures(game, card));
            }
            return Filters.or(cultures.toArray(new Culture[0]));
        }));
        parameterFilters.put("signetfrommemory", ((parameter, environment) -> actionContext -> {
            Set<Signet> signets = new HashSet<>();
            var game = actionContext.getGame();
            for (PhysicalCard card : actionContext.getCardsFromMemory(parameter)) {
                for (Signet signet : Signet.values()) {
                    if (game.getModifiersQuerying().hasSignet(game, card, signet))
                        signets.add(signet);
                }
            }
            return Filters.or(signets.toArray(new Signet[0]));
        }));
        parameterFilters.put("gametextcontains", ((parameter, environment) -> {
            final String searchText = parameter.toLowerCase();
            return actionContext -> (Filter) (game, physicalCard) -> {
                var gameText = physicalCard.getBlueprint().getGameText();
                return gameText != null && gameText.toLowerCase().contains(searchText);
            };
        }));
        // A card that attaches (a possession, an artifact, a condition borne by a character) and could be played
        // now on some card the inner filter accepts: its own requirements and twilight cost as for "Playable",
        // and a legal bearer among those cards.  For a card in a hand or a pile, in a requirement that goes
        // with "PlayCardFromDiscard ... on: <filter>" and the like (F-0140).  What the action will take from the
        // pool or add in threats before the play is counted as already gone, as PlayCardFromDiscard counts it.
        parameterFilters.put("playableon",
                (parameter, environment) -> {
                    final FilterableSource onSource = environment.getFilterFactory().generateFilter(parameter, environment);
                    return (actionContext) -> {
                        final Filterable on = onSource.getFilterable(actionContext);
                        final int twilightGoneFirst = actionContext.getUnpaidTwilightCost();
                        final int threatsAddedFirst = actionContext.getUnpaidThreatCost();
                        final Filter playable = Filters.playable(0, twilightGoneFirst, threatsAddedFirst, false, false, true);
                        return (Filter) (game, physicalCard) ->
                                physicalCard.getBlueprint().getValidTargetFilter(physicalCard.getOwner(), game, physicalCard) != null
                                        && playable.accepts(game, physicalCard)
                                        && PlayUtils.checkPlayRequirements(game, physicalCard, on, twilightGoneFirst, threatsAddedFirst, 0, false, false, true);
                    };
                });
        // A card in play that a card's effect could transfer now to some card the inner filter accepts: it may
        // be transferred at all, and one of those cards is an eligible bearer for it other than its own
        // (Filters.transferableTo).  For the requirement that goes with "Transfer ... where: choose(<filter>),
        // checkTarget: true", so that the offer is withheld when nothing could move (F-0324).
        parameterFilters.put("transferableto",
                (parameter, environment) -> {
                    final FilterableSource toSource = environment.getFilterFactory().generateFilter(parameter, environment);
                    return (actionContext) -> Filters.transferableTo(toSource.getFilterable(actionContext));
                });
        parameterFilters.put("hasattached",
                (parameter, environment) -> {
                    final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(parameter, environment);
                    return (actionContext) -> Filters.hasAttached(filterableSource.getFilterable(actionContext));
                });
        // hasAttachedCount(3,possession): bearing 3 or more possessions.  A last word override(hindered) has the
        // count take in hindered cards, as "override: hindered" does on an effect: hasAttachedCount(3,any,
        // override(hindered)) is "bearing 3 or more cards".  For a count that names nothing but "cards" or a side
        // (Ketura on Q-0098 and on Lying Counsel 15_85); anything more specific does not see a hindered card.
        parameterFilters.put("hasattachedcount",
                (parameter, environment) -> {
                    String[] parameterSplit = parameter.split(",", 2);
                    int count = Integer.parseInt(parameterSplit[0].trim());
                    Map<InactiveReason, Boolean> override = SpotOverride.NONE;
                    List<String> counted = new LinkedList<>();
                    for (String part : splitIntoFilters(parameterSplit[1])) {
                        if (part.startsWith("override(") && part.endsWith(")"))
                            override = FieldUtils.getSpotOverride(part.substring("override(".length(), part.length() - 1).trim(), "override");
                        else
                            counted.add(part);
                    }
                    final Map<InactiveReason, Boolean> countOverride = override;
                    final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(
                            counted.isEmpty() ? "any" : String.join(",", counted), environment);
                    return (actionContext) -> Filters.hasAttached(count, countOverride, filterableSource.getFilterable(actionContext));
                });
        parameterFilters.put("hasanyculturetokens", (parameter, environment) -> {
            int count = Integer.parseInt(parameter != null ? parameter : "1");
            return (actionContext) -> Filters.hasAnyCultureTokens(count);
        });
        parameterFilters.put("hasculturetoken", (parameter, environment) -> {
            final Culture culture = Culture.findCulture(parameter);
            if (culture == null)
                throw new InvalidCardDefinitionException("Unable to find culture for: " + parameter);
            final Token token = Token.findTokenForCulture(culture);
            if (token == null)
                throw new InvalidCardDefinitionException("Unable to find token for culture: " + parameter);

            return (actionContext) -> Filters.hasToken(token);
        });
        parameterFilters.put("hasculturetokencount", (parameter, environment) -> {
            String[] parameterSplit = parameter.split(",", 2);
            int count = Integer.parseInt(parameterSplit[0]);
            final Culture culture = Culture.findCulture(parameterSplit[1]);
            if (culture == null)
                throw new InvalidCardDefinitionException("Unable to find culture for: " + parameter);
            final Token token = Token.findTokenForCulture(culture);
            if (token == null)
                throw new InvalidCardDefinitionException("Unable to find token for culture: " + parameter);

            return (actionContext) -> Filters.hasToken(token, count);
        });
        parameterFilters.put("hasstacked",
                (parameter, environment) -> {
                    final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(parameter, environment);
                    return (actionContext) -> Filters.hasStacked(filterableSource.getFilterable(actionContext));
                });
        parameterFilters.put("hasstackedcount",
                (parameter, environment) -> {
                    String[] parameterSplit = parameter.split(",", 2);
                    int count = Integer.parseInt(parameterSplit[0]);
                    final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(parameterSplit[1], environment);
                    return (actionContext) -> Filters.hasStacked(count, filterableSource.getFilterable(actionContext));
                });

        parameterFilters.put("highestracecount",
                (parameter, environment) -> {
                    final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(parameter, environment);
                    return actionContext -> {
                        var game = actionContext.getGame();
                        final Filterable sourceFilterable = filterableSource.getFilterable(actionContext);

                        Map<Race, Integer> counts = new HashMap<>();
                        for (PhysicalCard card : Filters.filterActive(actionContext.getGame(), sourceFilterable)) {
                            for(var race : game.getModifiersQuerying().getRaces(game, card)) {
                                Integer count = counts.get(race);
                                if (count == null)
                                    count = 0;
                                counts.put(race, count + 1);
                            }
                        }

                        int highestCount = 0;
                        for (Integer value : counts.values()) {
                            if (value > highestCount)
                                highestCount = value;
                        }

                        Set<Race> highestNumberRaces = new HashSet<>();
                        for (Map.Entry<Race, Integer> raceIntegerEntry : counts.entrySet()) {
                            if (raceIntegerEntry.getValue() == highestCount)
                                highestNumberRaces.add(raceIntegerEntry.getKey());
                        }

                        return Filters.or(highestNumberRaces.toArray(new Race[0]));
                    };
                });
        parameterFilters.put("higheststrength",
                (parameter, environment) -> {
                    final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(parameter, environment);
                    return actionContext -> {
                        final Filterable sourceFilterable = filterableSource.getFilterable(actionContext);
                        return Filters.and(
                                sourceFilterable, Filters.strengthEqual(
                                        new SingleMemoryEvaluator(
                                                new Evaluator() {
                                                    @Override
                                                    public int evaluateExpression(LotroGame game, PhysicalCard cardAffected) {
                                                        int maxStrength = Integer.MIN_VALUE;
                                                        for (PhysicalCard card : Filters.filterActive(game, sourceFilterable))
                                                            maxStrength = Math.max(maxStrength, game.getModifiersQuerying().getStrength(game, card));
                                                        return maxStrength;
                                                    }
                                                }
                                        )
                                )
                        );
                    };
                });
        parameterFilters.put("loweststrength",
                (parameter, environment) -> {
                    final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(parameter, environment);
                    return actionContext -> {
                        final Filterable sourceFilterable = filterableSource.getFilterable(actionContext);
                        return Filters.and(
                                sourceFilterable, Filters.strengthEqual(
                                        new SingleMemoryEvaluator(
                                                new Evaluator() {
                                                    @Override
                                                    public int evaluateExpression(LotroGame game, PhysicalCard cardAffected) {
                                                        int minStrength = Integer.MAX_VALUE;
                                                        for (PhysicalCard card : Filters.filterActive(game, sourceFilterable))
                                                            minStrength = Math.min(minStrength, game.getModifiersQuerying().getStrength(game, card));
                                                        return minStrength;
                                                    }
                                                }
                                        )
                                )
                        );
                    };
                });
        parameterFilters.put("highesttwilight",
                (parameter, environment) -> {
                    final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(parameter, environment);
                    return actionContext -> {
                        final Filterable sourceFilterable = filterableSource.getFilterable(actionContext);
                        return Filters.and(
                                sourceFilterable, Filters.printedTwilightCost(
                                        new SingleMemoryEvaluator(
                                                new Evaluator() {
                                                    @Override
                                                    public int evaluateExpression(LotroGame game, PhysicalCard cardAffected) {
                                                        int maxTwilight = Integer.MIN_VALUE;
                                                        for (PhysicalCard card : Filters.filterActive(game, sourceFilterable))
                                                            maxTwilight = Math.max(maxTwilight, game.getModifiersQuerying().getCurrentTwilightCost(game, card));
                                                        return maxTwilight;
                                                    }
                                                }
                                        )
                                )
                        );
                    };
                });
        parameterFilters.put("lowesttwilight",
                (parameter, environment) -> {
                    final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(parameter, environment);
                    return actionContext -> {
                        final Filterable sourceFilterable = filterableSource.getFilterable(actionContext);
                        return Filters.and(
                                sourceFilterable, Filters.printedTwilightCost(
                                        new SingleMemoryEvaluator(
                                                new Evaluator() {
                                                    @Override
                                                    public int evaluateExpression(LotroGame game, PhysicalCard cardAffected) {
                                                        int minTwilight = Integer.MAX_VALUE;
                                                        for (PhysicalCard card : Filters.filterActive(game, sourceFilterable))
                                                            minTwilight = Math.min(minTwilight, game.getModifiersQuerying().getCurrentTwilightCost(game, card));
                                                        return minTwilight;
                                                    }
                                                }
                                        )
                                )
                        );
                    };
                });
        parameterFilters.put("inskirmishagainst",
                (parameter, environment) -> {
                    final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(parameter, environment);
                    return (actionContext) -> Filters.inSkirmishAgainst(filterableSource.getFilterable(actionContext));
                });
        parameterFilters.put("recentlyinskirmishagainst",
                (parameter, environment) -> {
                    final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(parameter, environment);
                    return (actionContext) -> Filters.recentlyInSkirmishAgainst(filterableSource.getFilterable(actionContext));
                });
        parameterFilters.put("inskirmishagainstatleast",
                (parameter, environment) -> {
                    String[] split = parameter.split(",", 2);
                    int count = Integer.parseInt(split[0]);
                    final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(split[1], environment);
                    return (actionContext) -> Filters.and(Filters.inSkirmish,
                            (Filter) (game, physicalCard) -> {
                                Filterable filterable = filterableSource.getFilterable(actionContext);
                                return Filters.filter(game, game.getGameState().getSkirmish().getShadowCharacters(), filterable).size() >= count;
                            });
                });
        parameterFilters.put("matchestwilight",
                (parameter, environment) -> {
                    if (parameter.startsWith("memory(") && parameter.endsWith(")")) {
                        String memory = parameter.substring(parameter.indexOf("(") + 1, parameter.lastIndexOf(")"));
                        return actionContext -> {
                            try {
                                final int value = Integer.parseInt(actionContext.getValueFromMemory(memory));
                                return Filters.printedTwilightCost(new ConstantEvaluator(value));
                            } catch (IllegalArgumentException ex) {
                                return Filters.minPrintedTwilightCost(0);
                            }
                        };
                    } else {
                        final ValueSource valueSource = ValueResolver.resolveEvaluator(parameter, environment);
                        return actionContext -> Filters.printedTwilightCost(valueSource.getEvaluator(actionContext));
                    }
                });
        parameterFilters.put("maxtwilight",
                (parameter, environment) -> {
                    if (parameter.startsWith("memory(") && parameter.endsWith(")")) {
                        String memory = parameter.substring(parameter.indexOf("(") + 1, parameter.lastIndexOf(")"));
                        return actionContext -> {
                            try {
                                final int value = Integer.parseInt(actionContext.getValueFromMemory(memory));
                                return Filters.maxPrintedTwilightCost(value);
                            } catch (IllegalArgumentException ex) {
                                return Filters.maxPrintedTwilightCost(100);
                            }
                        };
                    } else {
                        final ValueSource valueSource = ValueResolver.resolveEvaluator(parameter, environment);
                        return actionContext -> {
                            final int value = valueSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
                            return Filters.maxPrintedTwilightCost(value);
                        };
                    }
                });
        parameterFilters.put("mintwilight",
                (parameter, environment) -> {
                    if (parameter.startsWith("memory(") && parameter.endsWith(")")) {
                        String memory = parameter.substring(parameter.indexOf("(") + 1, parameter.lastIndexOf(")"));
                        return actionContext -> {
                            try {
                                final int value = Integer.parseInt(actionContext.getValueFromMemory(memory));
                                return Filters.minPrintedTwilightCost(value);
                            } catch (IllegalArgumentException ex) {
                                return Filters.minPrintedTwilightCost(0);
                            }
                        };
                    } else {
                        final ValueSource valueSource = ValueResolver.resolveEvaluator(parameter, environment);
                        return actionContext -> {
                            final int value = valueSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
                            return Filters.minPrintedTwilightCost(value);
                        };
                    }
                });

        parameterFilters.put("maxresistance",
                (parameter, environment) -> {
                    final ValueSource valueSource = ValueResolver.resolveEvaluator(parameter, environment);

                    return (actionContext) -> {
                        int amount = valueSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
                        return Filters.maxResistance(amount);
                    };
                });
        parameterFilters.put("minresistance",
                (parameter, environment) -> {
                    final ValueSource valueSource = ValueResolver.resolveEvaluator(parameter, environment);

                    return (actionContext) -> {
                        int amount = valueSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
                        return Filters.minResistance(amount);
                    };
                });
        parameterFilters.put("maxstrength",
                (parameter, environment) -> {
                    final ValueSource valueSource = ValueResolver.resolveEvaluator(parameter, environment);

                    return (actionContext) -> {
                        int amount = valueSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
                        return Filters.maxStrength(amount);
                    };
                });
        parameterFilters.put("minstrength",
                (parameter, environment) -> {
                    final ValueSource valueSource = ValueResolver.resolveEvaluator(parameter, environment);

                    return (actionContext) -> {
                        int amount = valueSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
                        return Filters.minStrength(amount);
                    };
                });

        parameterFilters.put("minvitality",
                (parameter, environment) -> {
                    final ValueSource valueSource = ValueResolver.resolveEvaluator(parameter, environment);
                    return (actionContext) -> {
                        int amount = valueSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
                        return Filters.minVitality(amount);
                    };
                });
        parameterFilters.put("maxvitality",
                (parameter, environment) -> {
                    final ValueSource valueSource = ValueResolver.resolveEvaluator(parameter, environment);
                    return (actionContext) -> {
                        int amount = valueSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);
                        return Filters.maxVitality(amount);
                    };
                });
        //One particular card in this game, by the name it was given with GameState.labelCard
        parameterFilters.put("alias",
                (parameter, environment) -> {
                    final String label = parameter.trim();
                    return (actionContext) -> (Filter)
                            (game, physicalCard) -> physicalCard == game.getGameState().getLabeledCard(label);
                });
        parameterFilters.put("memory",
                (parameter, environment) -> (actionContext) -> Filters.in(actionContext.getCardsFromMemory(parameter)));
        parameterFilters.put("name",
                (parameter, environment) -> {
                    String name = Names.SanitizeName(Sanitize(parameter));
                    return (actionContext) -> (Filter)
                            (game, physicalCard) -> name != null
                                    && physicalCard.getBlueprint().getSanitizedTitle() != null
                                    && name.equals(Sanitize(physicalCard.getBlueprint().getSanitizedTitle()));
                });
        parameterFilters.put("title", parameterFilters.get("name"));
        parameterFilters.put("namefrommemory",
                (parameter, environment) -> actionContext -> {
                    Set<String> titles = new HashSet<>();
                    for (PhysicalCard physicalCard : actionContext.getCardsFromMemory(parameter))
                        titles.add(physicalCard.getBlueprint().getSanitizedTitle());
                    return (Filter) (game, physicalCard) -> titles.contains(physicalCard.getBlueprint().getSanitizedTitle());
                });
        parameterFilters.put("titlefrommemory", parameterFilters.get("namefrommemory"));
        parameterFilters.put("nameinstackedon",
                (parameter, environment) -> {
                    final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(parameter, environment);
                    return actionContext -> {
                        final Filterable sourceFilterable = filterableSource.getFilterable(actionContext);
                        return (Filter) (game, physicalCard) -> {
                            for (PhysicalCard cardWithStack : Filters.filterActive(game, sourceFilterable)) {
                                for (PhysicalCard stackedCard : game.getGameState().getStackedCards(cardWithStack)) {
                                    if (stackedCard.getBlueprint().getSanitizedTitle().equals(physicalCard.getBlueprint().getSanitizedTitle()))
                                        return true;
                                }
                            }
                            return false;
                        };
                    };
                });
        parameterFilters.put("ownercontrols",
                (parameter, environment) -> {
                    var filter = environment.getFilterFactory().createFilter(parameter, environment);
                    return (actionContext) -> {
                        var game = actionContext.getGame();
                        var card = Filters.findFirstActive(game, filter.getFilterable(actionContext));
                        if(card == null || card.getCardController() == null)
                            return Filters.none;

                        return Filters.owner(card.getCardController());
                    };
                });
        parameterFilters.put("printedtwilightcostfrommemory",
                (parameter, environment) -> actionContext -> {
                    var card = actionContext.getCardFromMemory(parameter);
                    if(card == null)
                        return Filters.none;
                    var bp = card.getBlueprint();
                    if (bp.getCardType() == CardType.THE_ONE_RING || bp.getCardType() == CardType.MAP)
                        return Filters.none;

                    return Filters.printedTwilightCost(new ConstantEvaluator(bp.getTwilightCost()));
                });
        parameterFilters.put("race",
                (parameter, environment) -> {
                    if (parameter.startsWith("memory(") && parameter.endsWith(")")) {
                        return actionContext -> (Filter) (game, physicalCard) -> {
                            String memory = parameter.substring(parameter.indexOf("(") + 1, parameter.lastIndexOf(")"));
                            var races = new HashSet<Race>();
                            for(var memoryCard : actionContext.getCardsFromMemory(memory)) {
                                races.addAll(game.getModifiersQuerying().getRaces(game, memoryCard));
                            }
                            //Disjoint returns true if there are no elements in common between two collections
                            return !Collections.disjoint(races, game.getModifiersQuerying().getRaces(game, physicalCard));
                        };
                    } else if (parameter.equals("stored")) {
                        return actionContext -> (Filter) (game, physicalCard) -> {
                            final PhysicalCard.WhileInZoneData value = actionContext.getSource().getWhileInZoneData();
                            if (value != null)
                                return game.getModifiersQuerying().getRaces(game, physicalCard).contains(Race.valueOf(value.getValue()));
                            return false;
                        };
                    } else if (parameter.equals("cannotspot")) {
                        return actionContext -> (Filter) (game, physicalCard) -> {
                            var races = game.getModifiersQuerying().getRaces(game, physicalCard);
                            if(races.isEmpty())
                                return false;

                            for(var race : races) {
                                if(Filters.canSpot(game, actionContext.getPerformingPlayer(), 1, SpotOverride.NONE, race))
                                    return false;
                            }
                            return true;
                        };
                    }
                    throw new InvalidCardDefinitionException("Unknown race definition in filter: " + parameter
                            + ".  Do not use race() for races; instead just list the race by itself.");
                });
        // A card that ReplaceCardInPlayWithCardInDiscard can replace: the performing player's discard pile holds a
        // card that matches the given filter and has this card's title and another subtitle.
        parameterFilters.put("replaceablefromdiscard",
                (parameter, environment) -> {
                    final FilterableSource filterableSource = environment.getFilterFactory().generateFilter(parameter, environment);
                    return actionContext -> {
                        final Filterable replacementFilter = filterableSource.getFilterable(actionContext);
                        final String playerId = actionContext.getPerformingPlayer();
                        return (Filter) (game, physicalCard) -> {
                            var blueprint = physicalCard.getBlueprint();
                            for (PhysicalCard inDiscard : Filters.filter(game, game.getGameState().getDiscard(playerId), replacementFilter)) {
                                var other = inDiscard.getBlueprint();
                                if (Objects.equals(other.getSanitizedTitle(), blueprint.getSanitizedTitle())
                                        && !Objects.equals(other.getSanitizedSubtitle(), blueprint.getSanitizedSubtitle()))
                                    return true;
                            }
                            return false;
                        };
                    };
                });
        parameterFilters.put("regionnumber",
                (parameter, environment) -> {
                    int min, max;
                    if (parameter.contains("-")) {
                        final String[] split = parameter.split("-", 2);
                        min = Integer.parseInt(split[0]);
                        max = Integer.parseInt(split[1]);
                    } else {
                        min = max = Integer.parseInt(parameter);
                    }

                    if (min < 0 || max > 3 || max < min)
                        throw new InvalidCardDefinitionException("Region number definition is invalid: " + parameter);

                    return (actionContext) -> Filters.regionNumberBetweenInclusive(min, max);
                });
        parameterFilters.put("resistancelessthanfilter",
                (parameter, environment) -> {
                    FilterableSource filter = environment.getFilterFactory().createFilter(parameter, environment);
                    return (actionContext) -> {
                        int resistance = 0;
                        LotroGame game = actionContext.getGame();
                        for (PhysicalCard physicalCard : Filters.filterActive(game, filter.getFilterable(actionContext))) {
                            resistance += game.getModifiersQuerying().getResistance(game, physicalCard);
                        }
                        return Filters.maxResistance(resistance - 1);
                    };
                });

        parameterFilters.put("side", (parameter, environment) -> {
            final Side side = Side.Parse(parameter);
            if (side == null)
                throw new InvalidCardDefinitionException("Unable to find side for: " + parameter);

            return (actionContext) -> side;
        });
        parameterFilters.put("signet", (parameter, environment) -> {
            final Signet signet = Signet.findSignet(parameter);
            if (signet == null)
                throw new InvalidCardDefinitionException("Unable to find signet for: " + parameter);

            return (actionContext) -> signet;
        });
        parameterFilters.put("siteblock",
                (parameter, environment) -> {
                    final SitesBlock sitesBlock = SitesBlock.findBlock(parameter);
                    return (actionContext) -> Filters.siteBlock(sitesBlock);
                });
        parameterFilters.put("sitenumber",
                (parameter, environment) -> {
                    int min, max;
                    if (parameter.contains("-")) {
                        final String[] split = parameter.split("-", 2);
                        min = Integer.parseInt(split[0]);
                        max = Integer.parseInt(split[1]);
                    } else {
                        min = max = Integer.parseInt(parameter);
                    }

                    return (actionContext) -> Filters.siteNumberBetweenInclusive(min, max);
                });
        parameterFilters.put("strengthlessthanfilter",
                (parameter, environment) -> {
                    FilterableSource filter = environment.getFilterFactory().createFilter(parameter, environment);
                    return (actionContext) -> {
                        int strength = 0;
                        LotroGame game = actionContext.getGame();
                        for (PhysicalCard physicalCard : Filters.filterActive(game, filter.getFilterable(actionContext))) {
                            strength += game.getModifiersQuerying().getStrength(game, physicalCard);
                        }
                        return Filters.maxStrength(strength - 1);
                    };
                });
        parameterFilters.put("subtitle",
                (parameter, environment) -> {
                    String sub = Names.SanitizeName(Sanitize(parameter));
                    return (actionContext) -> (Filter)
                            (game, physicalCard) -> sub != null
                                    && physicalCard.getBlueprint().getSanitizedSubtitle() != null
                                    && sub.equals(Sanitize(physicalCard.getBlueprint().getSanitizedSubtitle()));
                });
        parameterFilters.put("subtitlefrommemory",
                (parameter, environment) -> actionContext -> {
                    Set<String> subtitles = new HashSet<>();
                    for (PhysicalCard physicalCard : actionContext.getCardsFromMemory(parameter))
                        subtitles.add(physicalCard.getBlueprint().getSanitizedSubtitle());
                    return (Filter) (game, physicalCard) -> subtitles.contains(physicalCard.getBlueprint().getSanitizedSubtitle());
                });
        parameterFilters.put("timeword", (parameter, environment) -> {
            final Timeword timeword = Timeword.findTimeword(parameter);
            if (timeword == null) {
                if(parameter.toLowerCase().equals("current")) {
                    return (actionContext -> Timeword.findByPhase(actionContext.getGame().getGameState().getCurrentPhase()));
                }
                throw new InvalidCardDefinitionException("Unable to find timeword for: " + parameter);
            }

            return (actionContext) -> timeword;
        });
        // "In <a player's> support area", of a card other than the one whose text it is: lying in that player's
        // support area or borne, at any depth, by a card that is (Ketura on Q-0101).  support(you),
        // support(opponent), support(free people), support(shadow), support(owner).  The plain word `support` is
        // "in a support area", anybody's; zone(support) is the card itself lying in the support zone.
        parameterFilters.put("support",
                (parameter, environment) -> {
                    if (parameter.equals("opponent"))
                        return actionContext -> Filters.inSupportAreaOfAnOpponentOf(actionContext.getPerformingPlayer());
                    final PlayerSource whose = PlayerResolver.resolvePlayer(parameter.equals("your") ? "you" : parameter);
                    return actionContext -> Filters.inSupportAreaOf(whose.getPlayer(actionContext));
                });
        parameterFilters.put("zone",
                (parameter, environment) -> {
                    final Zone zone = FieldUtils.getEnum(Zone.class, parameter, "parameter");
                    return actionContext -> zone;
                });
    }

    private void appendFilter(Filterable value) {
        appendFilter(Sanitize(value.toString()), Hyphenize(value.toString()), value);
    }

    private void appendFilter(String filterName, String hyphenName, Filterable value) {
        if (simpleFilters.containsKey(filterName))
            throw new RuntimeException("Duplicate filter name: " + filterName);
        simpleFilters.put(filterName, (actionContext) -> value);
        if (!hyphenName.equals(filterName))
            simpleFilters.put(hyphenName, (actionContext -> value));
    }

    public FilterableSource generateFilter(String value, CardGenerationEnvironment environment) throws
            InvalidCardDefinitionException {
        if (value == null)
            throw new InvalidCardDefinitionException("Filter not specified");
        String[] filterStrings = splitIntoFilters(value);
        if (filterStrings.length == 0)
            return (actionContext) -> Filters.any;
        if (filterStrings.length == 1)
            return createFilter(filterStrings[0], environment);

        FilterableSource[] filters = new FilterableSource[filterStrings.length];
        for (int i = 0; i < filters.length; i++)
            filters[i] = createFilter(filterStrings[i], environment);
        return (actionContext) -> {
            Filterable[] filter = new Filterable[filters.length];
            for (int i = 0; i < filter.length; i++) {
                filter[i] = filters[i].getFilterable(actionContext);
            }

            return Filters.and(filter);
        };
    }

    private String[] splitIntoFilters(String value) throws InvalidCardDefinitionException {
        List<String> parts = new LinkedList<>();
        final char[] chars = value.toCharArray();

        int depth = 0;
        StringBuilder sb = new StringBuilder();
        for (char ch : chars) {
            if (depth > 0) {
                if (ch == ')')
                    depth--;
                if (ch == '(')
                    depth++;
                sb.append(ch);
            } else {
                if (ch == ',') {
                    parts.add(sb.toString().trim());
                    sb = new StringBuilder();
                } else {
                    if (ch == ')')
                        throw new InvalidCardDefinitionException("Mismatched closing parenthesis in filter: " + value);
                    if (ch == '(')
                        depth++;
                    sb.append(ch);
                }
            }
        }

        if (depth != 0)
            throw new InvalidCardDefinitionException("Not matching number of opening and closing brackets: " + value);

        parts.add(sb.toString().trim());

        return parts.toArray(new String[0]);
    }

    private FilterableSource createFilter(String filterString, CardGenerationEnvironment environment) throws
            InvalidCardDefinitionException {
        if (filterString.contains("(") && filterString.endsWith(")")) {
            String filterName = filterString.substring(0, filterString.indexOf("("));
            String filterParameter = filterString.substring(filterString.indexOf("(") + 1, filterString.lastIndexOf(")"));
            return lookupFilter(Sanitize(filterName), filterParameter.trim().toLowerCase(), environment);
        }
        return lookupFilter(Sanitize(filterString), null, environment);
    }


    private FilterableSource lookupFilter(String name, String parameter, CardGenerationEnvironment environment) throws
            InvalidCardDefinitionException {
        if (parameter == null) {
            FilterableSource result = simpleFilters.get(Sanitize(name));
            if (result != null)
                return result;
        }

        final FilterableSourceProducer filterableSourceProducer = parameterFilters.get(Sanitize(name));
        if (filterableSourceProducer == null)
            throw new InvalidCardDefinitionException("Unable to find filter: " + name);

        return filterableSourceProducer.createFilterableSource(parameter, environment);
    }

    public static String Sanitize(String input) {
        return input
                .toLowerCase()
                .replace(" ", "")
                .replace("_", "");
    }

    public static String Hyphenize(String input) {
        return input
                .toLowerCase()
                .replace(" ", "-")
                .replace("_", "-");
    }
}
